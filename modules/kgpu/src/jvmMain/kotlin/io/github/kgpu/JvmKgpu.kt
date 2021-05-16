package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h
import io.github.kgpu.wgpuj.wgpu_h.*
import jdk.incubator.foreign.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong

object Platform {
    val isWindows = System.getProperty("os.name").contains("Windows")
    val isLinux = System.getProperty("os.name").contains("Linux")
    val isMac = System.getProperty("os.name").contains("Mac")
}

object CUtils {
    val NULL: MemoryAddress = wgpu_h.NULL()!!

    fun copyToNativeArray(values: LongArray, scope: NativeScope): MemoryAddress {
        if (values.isEmpty())
            return NULL

        return scope.allocateArray(MemoryLayouts.JAVA_LONG, values).address()
    }
}

fun Boolean.toNativeByte(): Byte {
    return if (this) {
        0x01
    } else {
        0x00
    }
}

actual object Kgpu {
    actual val undefined = null

    fun initGlfw() {
        GlfwHandler.glfwInit()
    }

    // TODO: Create proper logging API
    fun initializeLogging() {
        val callback = WGPULogCallback.allocate { level, msg ->
            val msgJvm = CLinker.toJavaStringRestricted(msg, StandardCharsets.UTF_8)
            val levelStr = when (level) {
                WGPULogLevel_Error() -> "Error"
                WGPULogLevel_Warn() -> "Warn"
                WGPULogLevel_Info() -> "Info"
                WGPULogLevel_Debug() -> "Debug"
                WGPULogLevel_Trace() -> "Trace"
                else -> "UnknownLevel($level)"
            }
            println("$levelStr: $msgJvm")
        }
        wgpuSetLogCallback(callback)
        wgpuSetLogLevel(WGPULogLevel_Warn())
    }

    /**
     * Extracts wgpu-native from the classpath and loads it for the
     * JVM to use. For this function to work, there must be a library
     * called "wgpu_native" in the root of the classpath
     */
    fun loadNativesFromClasspath() {
        val library = SharedLibraryLoader().load("wgpu_native")
        System.load(library.absolutePath)
    }

    actual fun runLoop(window: Window, func: () -> Unit) {
        while (!window.isCloseRequested()) {
            window.update()
            func()
        }

        GlfwHandler.terminate()
    }

    actual suspend fun requestAdapterAsync(window: Window?): Adapter {
        val output = AtomicLong()

        NativeScope.unboundedScope().use { scope ->
            val options = WGPURequestAdapterOptions.allocate(scope)
            val callback = WGPURequestAdapterCallback.allocate({ result: MemoryAddress, _: MemoryAddress? ->
                output.set(result.toRawLongValue())
            }, scope)

            WGPURequestAdapterOptions.`compatibleSurface$set`(options, window?.surface?.address() ?: CUtils.NULL)
            WGPURequestAdapterOptions.`nextInChain$set`(options, CUtils.NULL)
            wgpuInstanceRequestAdapter(CUtils.NULL, options, callback, CUtils.NULL)
        }

        return Adapter(Id(output.get()))
    }
}

actual class Adapter(val id: Id) {

    override fun toString(): String {
        return "Adapter$id"
    }

    actual suspend fun requestDeviceAsync(): Device {
        val tracePath = System.getenv("KGPU_TRACE_PATH") ?: null
        val output = AtomicLong()

        NativeScope.unboundedScope().use { scope ->
            val desc = WGPUDeviceDescriptor.allocate(scope)
            val deviceExtras = WGPUDeviceExtras.allocate(scope)
            val chainedStruct = WGPUDeviceExtras.`chain$slice`(deviceExtras)
            val callback = WGPURequestDeviceCallback.allocate({ result, _ ->
                output.set(result.toRawLongValue())
            }, scope)

            WGPUChainedStruct.`sType$set`(chainedStruct, WGPUSType_DeviceExtras())
            WGPUDeviceExtras.`maxBindGroups$set`(deviceExtras, 1)
            WGPUDeviceDescriptor.`nextInChain$set`(desc, deviceExtras.address())

            if (tracePath != null) {
                println("Trace Path Set: $tracePath")
                WGPUDeviceExtras.`tracePath$set`(deviceExtras, CLinker.toCString(tracePath).address())
            }

            wgpuAdapterRequestDevice(id.address(), desc, callback, CUtils.NULL)
        }

        return Device(Id(output.get()))
    }
}

actual class Device(val id: Id) {

    override fun toString(): String {
        return "Device$id"
    }

    actual fun createShaderModule(src: String): ShaderModule {
        return NativeScope.unboundedScope().use { scope ->
            val desc = WGPUShaderModuleDescriptor.allocate(scope)
            val wgsl = WGPUShaderModuleWGSLDescriptor.allocate(scope)
            val wgslChain = WGPUShaderModuleWGSLDescriptor.`chain$slice`(wgsl)

            WGPUChainedStruct.`next$set`(wgslChain, CUtils.NULL)
            WGPUChainedStruct.`sType$set`(wgslChain, WGPUSType_ShaderModuleWGSLDescriptor())
            WGPUShaderModuleWGSLDescriptor.`source$set`(wgsl, CLinker.toCString(src, scope).address())
            WGPUShaderModuleDescriptor.`nextInChain$set`(desc, wgsl.address())

            ShaderModule(Id(wgpuDeviceCreateShaderModule(id, desc)))
        }
    }

    actual fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline {
        return RenderPipeline(Id(NativeScope.unboundedScope().use { scope ->
            val fragmentDesc = if (desc.fragment != null) {
                val fragmentDesc = WGPUFragmentState.allocate(scope)
                val targets = WGPUColorTargetState.allocateArray(desc.fragment.targets.size, scope)
                desc.fragment.targets.forEachIndexed { index, target ->
                    if (target.blendState == null)
                        TODO("Null blend states are currently not supported")

                    val blendState = WGPUBlendState.allocate(scope)
                    val colorBlend = WGPUBlendState.`color$slice`(blendState)
                    val alphaBlend = WGPUBlendState.`alpha$slice`(blendState)
                    WGPUBlendComponent.`srcFactor$set`(colorBlend, target.blendState.color.srcFactor.nativeVal)
                    WGPUBlendComponent.`dstFactor$set`(colorBlend, target.blendState.color.dstFactor.nativeVal)
                    WGPUBlendComponent.`operation$set`(colorBlend, target.blendState.color.operation.nativeVal)
                    WGPUBlendComponent.`srcFactor$set`(alphaBlend, target.blendState.alpha.srcFactor.nativeVal)
                    WGPUBlendComponent.`dstFactor$set`(alphaBlend, target.blendState.alpha.dstFactor.nativeVal)
                    WGPUBlendComponent.`operation$set`(alphaBlend, target.blendState.alpha.operation.nativeVal)

                    WGPUColorTargetState.`format$set`(targets, index.toLong(), target.format.nativeVal)
                    WGPUColorTargetState.`writeMask$set`(targets, index.toLong(), target.writeMask.toInt())
                    WGPUColorTargetState.`blend$set`(targets, index.toLong(), blendState.address())
                }
                WGPUFragmentState.`entryPoint$set`(fragmentDesc, CLinker.toCString(desc.fragment.entryPoint).address())
                WGPUFragmentState.`module$set`(fragmentDesc, desc.fragment.module.id.address())
                WGPUFragmentState.`targets$set`(fragmentDesc, targets.address())
                WGPUFragmentState.`targetCount$set`(fragmentDesc, desc.fragment.targets.size)

                fragmentDesc
            } else {
                CUtils.NULL
            }

            val descriptor = WGPURenderPipelineDescriptor.allocate(scope)
            val vertexState = WGPURenderPipelineDescriptor.`vertex$slice`(descriptor)
            val primitiveState = WGPURenderPipelineDescriptor.`primitive$slice`(descriptor)
            val multisampleState = WGPURenderPipelineDescriptor.`multisample$slice`(descriptor)

            WGPURenderPipelineDescriptor.`label$set`(descriptor, CUtils.NULL)
            WGPURenderPipelineDescriptor.`layout$set`(descriptor, desc.layout.id.address())
            WGPUVertexState.`module$set`(vertexState, desc.vertex.module.id.address())
            WGPUVertexState.`entryPoint$set`(vertexState, CLinker.toCString(desc.vertex.entryPoint).address())
            // TODO: Buffers
            WGPUPrimitiveState.`topology$set`(primitiveState, desc.primitive.topology.nativeVal)
            WGPUPrimitiveState.`stripIndexFormat$set`(
                primitiveState,
                (desc.primitive.stripIndexFormat?.nativeVal ?: WGPUIndexFormat_Undefined())
            )
            WGPUPrimitiveState.`frontFace$set`(primitiveState, WGPUFrontFace_CCW())
            WGPUPrimitiveState.`cullMode$set`(primitiveState, desc.primitive.cullMode.nativeVal)

            WGPUMultisampleState.`count$set`(multisampleState, desc.multisample.count)
            WGPUMultisampleState.`mask$set`(multisampleState, desc.multisample.mask)
            WGPUMultisampleState.`alphaToCoverageEnabled$set`(
                multisampleState,
                desc.multisample.alphaToCoverageEnabled.toNativeByte()
            )

            WGPURenderPipelineDescriptor.`fragment$set`(descriptor, fragmentDesc.address())

            wgpuDeviceCreateRenderPipeline(id, descriptor)
        }))
    }

    actual fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout {
        return PipelineLayout(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUPipelineLayoutDescriptor.allocate(scope)
            WGPUPipelineLayoutDescriptor.`bindGroupLayouts$set`(descriptor, CUtils.copyToNativeArray(desc.ids, scope))
            WGPUPipelineLayoutDescriptor.`bindGroupLayoutCount$set`(descriptor, desc.ids.size)

            wgpuDeviceCreatePipelineLayout(id, descriptor)
        }))
    }

    actual fun createTexture(desc: TextureDescriptor): Texture {
        TODO()
    }

    actual fun createCommandEncoder(): CommandEncoder {
        return CommandEncoder(Id(NativeScope.unboundedScope().use { scope ->
            val desc = WGPUCommandEncoderDescriptor.allocate(scope)
            WGPUCommandEncoderDescriptor.`label$set`(desc, CLinker.toCString("CommandEncoder", scope).address())
            wgpuDeviceCreateCommandEncoder(id, desc.address())
        }))
    }

    actual fun getDefaultQueue(): Queue {
        return Queue(Id(wgpuDeviceGetQueue(id)))
    }

    actual fun createBuffer(desc: BufferDescriptor): Buffer {
        return Buffer(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUBufferDescriptor.allocate(scope)
            WGPUBufferDescriptor.`nextInChain$set`(descriptor, CUtils.NULL)
            WGPUBufferDescriptor.`usage$set`(descriptor, desc.usage)
            WGPUBufferDescriptor.`size$set`(descriptor, desc.size)
            WGPUBufferDescriptor.`mappedAtCreation$set`(descriptor, desc.mappedAtCreation.toNativeByte())

            wgpuDeviceCreateBuffer(id, descriptor)
        }), desc.size)
    }

    actual fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout {
        return BindGroupLayout(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUBindGroupLayoutDescriptor.allocate(scope)
            val entries = WGPUBindGroupLayoutEntry.allocateArray(desc.entries.size)
            desc.entries.forEachIndexed { indexInt, entry ->
                val index = indexInt.toLong()
                WGPUBindGroupLayoutEntry.`binding$set`(entries, index, entry.binding.toInt())
                WGPUBindGroupLayoutEntry.`visibility$set`(entries, index, entry.visibility.toInt())

                val bufferBinding = WGPUBindGroupLayoutEntry.`buffer$slice`(entries)
                val samplerBinding = WGPUBindGroupLayoutEntry.`sampler$slice`(entries)
                val textureBinding = WGPUBindGroupLayoutEntry.`texture$slice`(entries)
                val storageTextureBinding = WGPUBindGroupLayoutEntry.`storageTexture$slice`(entries)

                WGPUBufferBindingLayout.`type$set`(bufferBinding, index, WGPUBufferBindingType_Undefined())
                WGPUSamplerBindingLayout.`type$set`(samplerBinding, index, WGPUSamplerBindingType_Undefined())
                WGPUTextureBindingLayout.`sampleType$set`(textureBinding, index, WGPUTextureSampleType_Undefined())
                WGPUStorageTextureBindingLayout.`access$set`(
                    storageTextureBinding,
                    index,
                    WGPUStorageTextureAccess_Undefined()
                )

                entry.bindingLayout.intoNative(
                    index,
                    bufferBinding,
                    samplerBinding,
                    textureBinding,
                    storageTextureBinding
                )
            }

            WGPUBindGroupLayoutDescriptor.`entries$set`(descriptor, entries.address())
            WGPUBindGroupLayoutDescriptor.`entryCount$set`(descriptor, desc.entries.size)


            wgpuDeviceCreateBindGroupLayout(id, descriptor)
        }))
    }

    actual fun createBindGroup(desc: BindGroupDescriptor): BindGroup {
        return BindGroup(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUBindGroupDescriptor.allocate(scope)
            val entries = WGPUBindGroupEntry.allocateArray(desc.entries.size, scope)
            desc.entries.forEachIndexed { indexInt, entry ->
                val index = indexInt.toLong()
                WGPUBindGroupEntry.`binding$set`(entries, index, entry.binding.toInt())

                entry.resource.intoBindingResource(entries, index)
            }

            WGPUBindGroupDescriptor.`layout$set`(descriptor, desc.layout.id.address())
            WGPUBindGroupDescriptor.`entries$set`(descriptor, entries.address())
            WGPUBindGroupDescriptor.`entryCount$set`(descriptor, desc.entries.size)

            wgpuDeviceCreateBindGroup(id, descriptor.address())
        }))
    }

    actual fun createSampler(desc: SamplerDescriptor): Sampler {
        TODO()
    }

    actual fun createComputePipeline(desc: ComputePipelineDescriptor): ComputePipeline {
        return ComputePipeline(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUComputePipelineDescriptor.allocate(scope)
            val stage = WGPUComputePipelineDescriptor.`computeStage$slice`(descriptor)

            WGPUComputePipelineDescriptor.`layout$set`(descriptor, desc.layout.id.address())
            WGPUProgrammableStageDescriptor.`module$set`(stage, desc.computeStage.module.id.address())
            WGPUProgrammableStageDescriptor.`entryPoint$set`(
                stage,
                CLinker.toCString(desc.computeStage.entryPoint, scope).address()
            )

            wgpuDeviceCreateComputePipeline(id, descriptor.address())
        }))
    }
}

actual class ShaderModule(val id: Id) {

    override fun toString(): String {
        return "ShaderModule$id"
    }
}

actual class Extent3D actual constructor(width: Long, height: Long, depth: Long) {

}

actual class Origin3D actual constructor(x: Long, y: Long, z: Long) {

}

actual class TextureDescriptor
actual constructor(
    size: Extent3D,
    mipLevelCount: Long,
    sampleCount: Int,
    dimension: TextureDimension,
    format: TextureFormat,
    usage: Long
) {
}

actual class Texture(val id: Long) {

    override fun toString(): String {
        return "Texture$id"
    }

    actual fun createView(desc: TextureViewDescriptor?): TextureView {
        TODO()
    }

    actual fun destroy() {
        TODO()
    }
}

actual class TextureViewDescriptor
actual constructor(
    format: TextureFormat,
    dimension: TextureViewDimension,
    aspect: TextureAspect,
    baseMipLevel: Long,
    mipLevelCount: Long,
    baseArrayLayer: Long,
    arrayLayerCount: Long
)

actual class TextureView(val id: Id) : IntoBindingResource {

    actual fun destroy() {
        TODO()

    }

    override fun intoBindingResource(entries: MemorySegment, index: Long) {
        TODO()
    }

    override fun toString(): String {
        return "TextureView$id"
    }
}

actual class SwapChainDescriptor
actual constructor(val device: Device, val format: TextureFormat, val usage: Long)

actual class SwapChain(val id: Id, private val window: Window) {

    private val size = window.windowSize

    override fun toString(): String {
        return "SwapChain$id"
    }

    actual fun getCurrentTextureView(): TextureView {
        return TextureView(Id(wgpuSwapChainGetCurrentTextureView(id)))
    }

    actual fun present() {
        wgpuSwapChainPresent(id)
    }

    actual fun isOutOfDate(): Boolean {
        return window.windowSize != size
    }
}

actual class Queue(val id: Id) {

    override fun toString(): String {
        return "Queue$id"
    }

    actual fun submit(vararg cmdBuffers: CommandBuffer) {
        NativeScope.unboundedScope().use { scope ->
            val bufferIds = CUtils.copyToNativeArray(cmdBuffers.map { it.id.id }.toLongArray(), scope)

            wgpuQueueSubmit(id, cmdBuffers.size, bufferIds)
        }
    }

    actual fun writeBuffer(
        buffer: Buffer, data: ByteArray, offset: Long, dataOffset: Long, size: Long
    ) {
        TODO()
    }
}

actual class BufferDescriptor
actual constructor(
    val label: String, val size: Long, val usage: Int, val mappedAtCreation: Boolean
) {
}

actual class Buffer(val id: Id, actual val size: Long) : IntoBindingResource {

    override fun intoBindingResource(entries: MemorySegment, index: Long) {
        TODO()
    }

    override fun toString(): String {
        return "Buffer$id"
    }

    actual fun getMappedData(start: Long, size: Long): BufferData {
        val ptr = wgpuBufferGetMappedRange(id, start, size)


        return BufferData(ptr.asSegmentRestricted(size))
    }

    actual fun unmap() {
        wgpuBufferUnmap(id)
    }

    actual fun destroy() {
        wgpuBufferDestroy(id)
    }

    actual suspend fun mapReadAsync(device: Device): BufferData {
        val callback = WGPUBufferMapCallback.allocate { _, _ -> }
        wgpuBufferMapAsync(id.address(), WGPUMapMode_Read(), 0, size, callback, CUtils.NULL)
        wgpuDevicePoll(device.id.address(), true.toNativeByte())

        return getMappedData(0, size)
    }
}

actual class BufferData(val data: MemorySegment) {

    actual fun putBytes(bytes: ByteArray, offset: Int) {
        data.asByteBuffer().put(offset, bytes)
    }

    actual fun getBytes(): ByteArray {
        val buffer = ByteArray(data.byteSize().toInt())
        data.asByteBuffer().get(buffer)

        return buffer
    }
}

actual class SamplerDescriptor
actual constructor(
    compare: CompareFunction?,
    addressModeU: AddressMode,
    addressModeV: AddressMode,
    addressModeW: AddressMode,
    magFilter: FilterMode,
    minFilter: FilterMode,
    mipmapFilter: FilterMode,
    lodMinClamp: kotlin.Float,
    lodMaxClamp: kotlin.Float,
    maxAnisotrophy: Short
)

actual class Sampler(val id: Long) : IntoBindingResource {

    override fun intoBindingResource(entries: MemorySegment, index: Long) {
        TODO()
    }

    override fun toString(): String {
        return "Sampler$id"
    }
}