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

fun <T> Array<T>.mapToNativeEntries(
    scope: NativeScope,
    nativeSize: Long,
    allocator: (Int, NativeScope) -> MemorySegment,
    action: (T, MemorySegment) -> Unit
) : MemorySegment {
    val nativeArray = allocator(this.size, scope)
    forEachIndexed { index, jvmEntry ->
        val nativeEntry = nativeArray.asSlice(nativeSize * index)

        action(jvmEntry, nativeEntry)
    }

    return nativeArray
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

            val buffers = desc.vertex.buffers.mapToNativeEntries(
                scope,
                WGPUVertexBufferLayout.sizeof(),
                WGPUVertexBufferLayout::allocateArray,
            ) {jvmBufferLayout, nativeBufferLayout ->
                val attributes = jvmBufferLayout.attributes.mapToNativeEntries(
                    scope,
                    WGPUVertexAttribute.sizeof(),
                    WGPUVertexAttribute::allocateArray,
                ) {jvmAttribute, nativeAttribute ->
                    WGPUVertexAttribute.`shaderLocation$set`(nativeAttribute, jvmAttribute.shaderLocation)
                    WGPUVertexAttribute.`format$set`(nativeAttribute, jvmAttribute.format.nativeVal)
                    WGPUVertexAttribute.`offset$set`(nativeAttribute, jvmAttribute.offset)
                }

                WGPUVertexBufferLayout.`arrayStride$set`(nativeBufferLayout, jvmBufferLayout.arrayStride)
                WGPUVertexBufferLayout.`stepMode$set`(nativeBufferLayout, jvmBufferLayout.stepMode.nativeVal)
                WGPUVertexBufferLayout.`attributeCount$set`(nativeBufferLayout, jvmBufferLayout.attributes.size)
                WGPUVertexBufferLayout.`attributes$set`(nativeBufferLayout, attributes.address())
            }

            val descriptor = WGPURenderPipelineDescriptor.allocate(scope)
            val vertexState = WGPURenderPipelineDescriptor.`vertex$slice`(descriptor)
            val primitiveState = WGPURenderPipelineDescriptor.`primitive$slice`(descriptor)
            val multisampleState = WGPURenderPipelineDescriptor.`multisample$slice`(descriptor)

            WGPURenderPipelineDescriptor.`label$set`(descriptor, CUtils.NULL)
            WGPURenderPipelineDescriptor.`layout$set`(descriptor, desc.layout.id.address())

            WGPUVertexState.`module$set`(vertexState, desc.vertex.module.id.address())
            WGPUVertexState.`entryPoint$set`(vertexState, CLinker.toCString(desc.vertex.entryPoint).address())
            WGPUVertexState.`buffers$set`(vertexState, buffers.address())
            WGPUVertexState.`bufferCount$set`(vertexState, desc.vertex.buffers.size)

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
        return Texture(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUTextureDescriptor.allocate(scope)
            val size = WGPUTextureDescriptor.`size$slice`(descriptor)

            WGPUTextureDescriptor.`usage$set`(descriptor, desc.usage.toInt())
            WGPUTextureDescriptor.`dimension$set`(descriptor, desc.dimension.nativeVal)
            WGPUTextureDescriptor.`format$set`(descriptor, desc.format.nativeVal)
            WGPUTextureDescriptor.`mipLevelCount$set`(descriptor, desc.mipLevelCount.toInt())
            WGPUTextureDescriptor.`sampleCount$set`(descriptor, desc.sampleCount)
            WGPUExtent3D.`width$set`(size, desc.size.width.toInt())
            WGPUExtent3D.`height$set`(size, desc.size.height.toInt())
            WGPUExtent3D.`depth$set`(size, desc.size.depth.toInt())

            wgpuDeviceCreateTexture(id, descriptor)
        }))
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
            val entries = desc.entries.mapToNativeEntries(
                scope,
                WGPUBindGroupLayoutEntry.sizeof(),
                WGPUBindGroupLayoutEntry::allocateArray,
            ) { jvmEntry, nativeEntry ->
                WGPUBindGroupLayoutEntry.`binding$set`(nativeEntry, jvmEntry.binding.toInt())
                WGPUBindGroupLayoutEntry.`visibility$set`(nativeEntry, jvmEntry.visibility.toInt())

                val bufferBinding = WGPUBindGroupLayoutEntry.`buffer$slice`(nativeEntry)
                val samplerBinding = WGPUBindGroupLayoutEntry.`sampler$slice`(nativeEntry)
                val textureBinding = WGPUBindGroupLayoutEntry.`texture$slice`(nativeEntry)
                val storageTextureBinding = WGPUBindGroupLayoutEntry.`storageTexture$slice`(nativeEntry)

                WGPUBufferBindingLayout.`type$set`(bufferBinding, WGPUBufferBindingType_Undefined())
                WGPUSamplerBindingLayout.`type$set`(samplerBinding, WGPUSamplerBindingType_Undefined())
                WGPUTextureBindingLayout.`sampleType$set`(textureBinding, WGPUTextureSampleType_Undefined())
                WGPUStorageTextureBindingLayout.`access$set`(
                    storageTextureBinding,
                    WGPUStorageTextureAccess_Undefined()
                )

                jvmEntry.bindingLayout.intoNative(
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
            val entries = desc.entries.mapToNativeEntries(
                scope,
                WGPUBindGroupEntry.sizeof(),
                WGPUBindGroupEntry::allocateArray
            ) { jvmEntry, nativeEntry ->
                WGPUBindGroupEntry.`binding$set`(nativeEntry, jvmEntry.binding.toInt())
                jvmEntry.resource.intoBindingResource(nativeEntry)
            }

            WGPUBindGroupDescriptor.`layout$set`(descriptor, desc.layout.id.address())
            WGPUBindGroupDescriptor.`entries$set`(descriptor, entries.address())
            WGPUBindGroupDescriptor.`entryCount$set`(descriptor, desc.entries.size)

            wgpuDeviceCreateBindGroup(id, descriptor.address())
        }))
    }

    actual fun createSampler(desc: SamplerDescriptor): Sampler {
        return Sampler(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUSamplerDescriptor.allocate(scope)

            WGPUSamplerDescriptor.`addressModeU$set`(descriptor, desc.addressModeU.nativeVal)
            WGPUSamplerDescriptor.`addressModeV$set`(descriptor, desc.addressModeV.nativeVal)
            WGPUSamplerDescriptor.`addressModeW$set`(descriptor, desc.addressModeW.nativeVal)
            WGPUSamplerDescriptor.`magFilter$set`(descriptor, desc.magFilter.nativeVal)
            WGPUSamplerDescriptor.`minFilter$set`(descriptor, desc.minFilter.nativeVal)
            WGPUSamplerDescriptor.`mipmapFilter$set`(descriptor, desc.mipmapFilter.nativeVal)
            WGPUSamplerDescriptor.`lodMinClamp$set`(descriptor, desc.lodMinClamp)
            WGPUSamplerDescriptor.`lodMaxClamp$set`(descriptor, desc.lodMaxClamp)
            WGPUSamplerDescriptor.`compare$set`(descriptor, desc.compare?.nativeVal ?: WGPUCompareFunction_Undefined())
            WGPUSamplerDescriptor.`maxAnisotropy$set`(descriptor, desc.maxAnisotrophy)

            wgpuDeviceCreateSampler(id, descriptor.address())
        }))
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

actual class Extent3D actual constructor(val width: Long, val height: Long, val depth: Long) {
    fun toNative(scope: NativeScope): MemorySegment {
        val native = WGPUExtent3D.allocate(scope)
        WGPUExtent3D.`width$set`(native, width.toInt())
        WGPUExtent3D.`height$set`(native, height.toInt())
        WGPUExtent3D.`depth$set`(native, depth.toInt())

        return native
    }
}

actual class Origin3D actual constructor(val x: Long, val y: Long, val z: Long)

actual class TextureDescriptor
actual constructor(
    val size: Extent3D,
    val mipLevelCount: Long,
    val sampleCount: Int,
    val dimension: TextureDimension,
    val format: TextureFormat,
    val usage: Long
) {
}

actual class Texture(val id: Id) {

    override fun toString(): String {
        return "Texture$id"
    }

    actual fun createView(desc: TextureViewDescriptor?): TextureView {
        return TextureView(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = WGPUTextureViewDescriptor.allocate(scope)
            WGPUTextureViewDescriptor.`format$set`(descriptor, desc?.format?.nativeVal ?: WGPUTextureFormat_Undefined())
            WGPUTextureViewDescriptor.`dimension$set`(
                descriptor,
                desc?.dimension?.nativeVal ?: WGPUTextureViewDimension_Undefined()
            )
            WGPUTextureViewDescriptor.`aspect$set`(descriptor, desc?.aspect?.nativeVal ?: WGPUTextureAspect_All())
            WGPUTextureViewDescriptor.`baseMipLevel$set`(descriptor, desc?.baseMipLevel?.toInt() ?: 0)
            WGPUTextureViewDescriptor.`mipLevelCount$set`(descriptor, desc?.mipLevelCount?.toInt() ?: 0)
            WGPUTextureViewDescriptor.`baseArrayLayer$set`(descriptor, desc?.baseArrayLayer?.toInt() ?: 0)
            WGPUTextureViewDescriptor.`arrayLayerCount$set`(descriptor, desc?.arrayLayerCount?.toInt() ?: 0)

            wgpuTextureCreateView(id, descriptor)
        }))
    }

    actual fun destroy() {
        TODO()
    }
}

actual class TextureViewDescriptor
actual constructor(
    val format: TextureFormat,
    val dimension: TextureViewDimension,
    val aspect: TextureAspect,
    val baseMipLevel: Long,
    val mipLevelCount: Long,
    val baseArrayLayer: Long,
    val arrayLayerCount: Long
)

actual class TextureView(val id: Id) : IntoBindingResource {

    actual fun destroy() {
        TODO()

    }

    override fun intoBindingResource(entry: MemorySegment) {
        WGPUBindGroupEntry.`textureView$set`(entry, id.address())
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

    override fun intoBindingResource(entry: MemorySegment) {
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
        println("Warning: wgpuBufferDestroy currently not implemented")
//        wgpuBufferDestroy(id)
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
    val compare: CompareFunction?,
    val addressModeU: AddressMode,
    val addressModeV: AddressMode,
    val addressModeW: AddressMode,
    val magFilter: FilterMode,
    val minFilter: FilterMode,
    val mipmapFilter: FilterMode,
    val lodMinClamp: kotlin.Float,
    val lodMaxClamp: kotlin.Float,
    val maxAnisotrophy: Short
)

actual class Sampler(val id: Id) : IntoBindingResource {

    override fun intoBindingResource(entry: MemorySegment) {
        WGPUBindGroupEntry.`sampler$set`(entry, id.address())
    }

    override fun toString(): String {
        return "Sampler$id"
    }
}