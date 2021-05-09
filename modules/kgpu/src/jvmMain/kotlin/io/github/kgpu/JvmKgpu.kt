package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h
import io.github.kgpu.wgpuj.wgpu_h.*
import jdk.incubator.foreign.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong

object Platform {
    val isWindows = System.getProperty("os.name").contains("Windows")
    val isLinux = System.getProperty("os.name").contains("Linux")
    val isMac = System.getProperty("os.name").contains("Mac")
}

object CUtils {
    val NULL: MemoryAddress = wgpu_h.NULL()!!

    fun copyToNativeArray(values: LongArray): MemoryAddress {
        if (values.isEmpty())
            return NULL

        val layouts = MemorySegment.allocateNative(values.size * Primitives.LONG_BYTES)
        layouts.copyFrom(MemorySegment.ofArray(values))

        return layouts.address()
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
        val options = WGPURequestAdapterOptions.allocate()
        val output = AtomicLong()
        val callback = WGPURequestAdapterCallback.allocate { result: MemoryAddress, _: MemoryAddress? ->
            output.set(result.toRawLongValue())
        }

        WGPURequestAdapterOptions.`compatibleSurface$set`(options, window?.surface ?: CUtils.NULL)
        WGPURequestAdapterOptions.`nextInChain$set`(options, CUtils.NULL)
        wgpuInstanceRequestAdapter(CUtils.NULL, options, callback, CUtils.NULL)

        return Adapter(Id(output.get()))
    }
}

actual class Adapter(val id: Id) {

    override fun toString(): String {
        return "Adapter$id"
    }

    actual suspend fun requestDeviceAsync(): Device {
        val desc = WGPUDeviceDescriptor.allocate()
        val deviceExtras = WGPUDeviceExtras.allocate()
        val chainedStruct = WGPUDeviceExtras.`chain$slice`(deviceExtras)
        val tracePath = System.getenv("KGPU_TRACE_PATH") ?: null
        val output = AtomicLong()
        val callback = WGPURequestDeviceCallback.allocate { result, _ ->
            output.set(result.toRawLongValue())
        }

        WGPUChainedStruct.`sType$set`(chainedStruct, WGPUSType_DeviceExtras())
        WGPUDeviceExtras.`maxBindGroups$set`(deviceExtras, 1)
        WGPUDeviceDescriptor.`nextInChain$set`(desc, deviceExtras.address())

        if (tracePath != null) {
            println("Trace Path Set: $tracePath")
            WGPUDeviceExtras.`tracePath$set`(deviceExtras, CLinker.toCString(tracePath).address())
        }

        wgpuAdapterRequestDevice(id.address(), desc, callback, CUtils.NULL)

        return Device(Id(output.get()))
    }
}

actual class Device(val id: Id) {

    override fun toString(): String {
        return "Device$id"
    }

    actual fun createShaderModule(src: String): ShaderModule {
        val desc = WGPUShaderModuleDescriptor.allocate()
        val wgsl = WGPUShaderModuleWGSLDescriptor.allocate()
        val wgslChain = WGPUShaderModuleWGSLDescriptor.`chain$slice`(wgsl)

        WGPUChainedStruct.`next$set`(wgslChain, CUtils.NULL)
        WGPUChainedStruct.`sType$set`(wgslChain, WGPUSType_ShaderModuleWGSLDescriptor())
        WGPUShaderModuleWGSLDescriptor.`source$set`(wgsl, CLinker.toCString(src).address())
        WGPUShaderModuleDescriptor.`nextInChain$set`(desc, wgsl.address())

        return ShaderModule(Id(wgpuDeviceCreateShaderModule(id, desc)))
    }

    actual fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline {
        val fragmentDesc = if (desc.fragmentStage != null) {
            val fragmentDesc = WGPUFragmentState.allocate()
            val targets = WGPUColorTargetState.allocateArray(desc.fragmentStage.targets.size)
            desc.fragmentStage.targets.forEachIndexed { index, target ->
                if (target.blendState == null)
                    TODO("Null blend states are currently not supported")

                val blendState = WGPUBlendState.allocate()
                val colorBlend = WGPUBlendState.`color$slice`(blendState)
                val alphaBlend = WGPUBlendState.`alpha$slice`(blendState)
                WGPUBlendComponent.`srcFactor$set`(colorBlend, target.blendState.color.srcFactor.ordinal)
                WGPUBlendComponent.`dstFactor$set`(colorBlend, target.blendState.color.dstFactor.ordinal)
                WGPUBlendComponent.`operation$set`(colorBlend, target.blendState.color.operation.ordinal)
                WGPUBlendComponent.`srcFactor$set`(alphaBlend, target.blendState.alpha.srcFactor.ordinal)
                WGPUBlendComponent.`dstFactor$set`(alphaBlend, target.blendState.alpha.dstFactor.ordinal)
                WGPUBlendComponent.`operation$set`(alphaBlend, target.blendState.alpha.operation.ordinal)

                WGPUColorTargetState.`format$set`(targets, index.toLong(), target.format.nativeVal)
                WGPUColorTargetState.`writeMask$set`(targets, index.toLong(), target.writeMask.toInt())
                WGPUColorTargetState.`blend$set`(targets, index.toLong(), blendState.address())
            }
            WGPUFragmentState.`entryPoint$set`(fragmentDesc, CLinker.toCString(desc.fragmentStage.entryPoint).address())
            WGPUFragmentState.`module$set`(fragmentDesc, desc.fragmentStage.module.id.address())
            WGPUFragmentState.`targets$set`(fragmentDesc, targets.address())
            WGPUFragmentState.`targetCount$set`(fragmentDesc, desc.fragmentStage.targets.size)

            fragmentDesc
        } else {
            CUtils.NULL
        }

        val descriptor = WGPURenderPipelineDescriptor.allocate()
        val vertexState = WGPURenderPipelineDescriptor.`vertex$slice`(descriptor)
        val primitiveState = WGPURenderPipelineDescriptor.`primitive$slice`(descriptor)
        val multisampleState = WGPURenderPipelineDescriptor.`multisample$slice`(descriptor)

        WGPURenderPipelineDescriptor.`label$set`(descriptor, CLinker.toCString("RenderPipeline").address())
        WGPURenderPipelineDescriptor.`layout$set`(descriptor, desc.layout.id.address())
        WGPUVertexState.`module$set`(vertexState, desc.vertexStage.module.id.address())
        WGPUVertexState.`entryPoint$set`(vertexState, CLinker.toCString(desc.vertexStage.entryPoint).address())
        // TODO: Buffers
        WGPUPrimitiveState.`topology$set`(primitiveState, desc.primitiveTopology.topology.ordinal)
        WGPUPrimitiveState.`stripIndexFormat$set`(
            primitiveState,
            (desc.primitiveTopology.stripIndexFormat?.ordinal ?: WGPUIndexFormat_Undefined())
        )
        WGPUPrimitiveState.`frontFace$set`(primitiveState, WGPUFrontFace_CCW())
        WGPUPrimitiveState.`cullMode$set`(primitiveState, desc.primitiveTopology.cullMode.ordinal)

        WGPUMultisampleState.`count$set`(multisampleState, desc.multisampleState.count)
        WGPUMultisampleState.`mask$set`(multisampleState, desc.multisampleState.mask)
        WGPUMultisampleState.`alphaToCoverageEnabled$set`(
            multisampleState,
            desc.multisampleState.alphaToCoverageEnabled.toNativeByte()
        )

        WGPURenderPipelineDescriptor.`fragment$set`(descriptor, fragmentDesc.address())

        return RenderPipeline(Id(wgpuDeviceCreateRenderPipeline(id, descriptor)))
    }

    actual fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout {
        val descriptor = WGPUPipelineLayoutDescriptor.allocate()
        WGPUPipelineLayoutDescriptor.`bindGroupLayouts$set`(descriptor, CUtils.copyToNativeArray(desc.ids))
        WGPUPipelineLayoutDescriptor.`bindGroupLayoutCount$set`(descriptor, desc.ids.size)

        return PipelineLayout(Id(wgpuDeviceCreatePipelineLayout(id, descriptor)))
    }

    actual fun createTexture(desc: TextureDescriptor): Texture {
        TODO()
    }

    actual fun createCommandEncoder(): CommandEncoder {
        TODO()
    }

    actual fun getDefaultQueue(): Queue {
        TODO()
    }

    actual fun createBuffer(desc: BufferDescriptor): Buffer {
        val descriptor = WGPUBufferDescriptor.allocate()
        WGPUBufferDescriptor.`nextInChain$set`(descriptor, CUtils.NULL)
        WGPUBufferDescriptor.`usage$set`(descriptor, desc.usage)
        WGPUBufferDescriptor.`size$set`(descriptor, desc.size)
        WGPUBufferDescriptor.`mappedAtCreation$set`(descriptor, desc.mappedAtCreation.toNativeByte())

        return Buffer(Id(wgpuDeviceCreateBuffer(id, descriptor)), desc.size)
    }

    actual fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout {
        TODO()

    }

    actual fun createBindGroup(desc: BindGroupDescriptor): BindGroup {
        TODO()
    }

    actual fun createSampler(desc: SamplerDescriptor): Sampler {
        TODO()
    }

    actual fun createComputePipeline(desc: ComputePipelineDescriptor): ComputePipeline {
        TODO()
    }
}

actual class CommandEncoder(val id: Id) {

    override fun toString(): String {
        return "CommandEncoder$id"
    }

    actual fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder {
        TODO()
    }

    actual fun finish(): CommandBuffer {
        TODO()
    }

    actual fun copyBufferToTexture(
        source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D
    ) {
        TODO()
    }

    actual fun beginComputePass(): ComputePassEncoder {
        TODO()
    }

    actual fun copyBufferToBuffer(
        source: Buffer, destination: Buffer, size: Long, sourceOffset: Int, destinationOffset: Int
    ) {
        TODO()
    }

    actual fun copyTextureToBuffer(source: TextureCopyView, dest: BufferCopyView, size: Extent3D) {
        TODO()
    }
}

actual class RenderPassEncoder {

    override fun toString(): String {
        return "RenderPassEncoder"
    }

    actual fun setPipeline(pipeline: RenderPipeline) {
        TODO()
    }

    actual fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        TODO()
    }

    actual fun endPass() {
        TODO()
    }

    actual fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long, size: Long) {
        TODO()
    }

    actual fun drawIndexed(
        indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int
    ) {
        TODO()
    }

    actual fun setIndexBuffer(buffer: Buffer, indexFormat: IndexFormat, offset: Long, size: Long) {
        TODO()
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        TODO()
    }
}

actual class ComputePassEncoder() {

    override fun toString(): String {
        return "ComputePassEncoder"
    }

    actual fun setPipeline(pipeline: ComputePipeline) {
        TODO()
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        TODO()
    }

    actual fun dispatch(x: Int, y: Int, z: Int) {
        TODO()
    }

    actual fun endPass() {
        TODO()
    }
}

actual class ShaderModule(val id: Id) {

    override fun toString(): String {
        return "ShaderModule$id"
    }
}

actual class ProgrammableStageDescriptor
actual constructor(val module: ShaderModule, val entryPoint: String) {
}

actual class BindGroupLayoutEntry
actual constructor(
    binding: Long,
    visibility: Long,
    type: BindingType,
    hasDynamicOffset: kotlin.Boolean,
    viewDimension: TextureViewDimension?,
    textureComponentType: TextureComponentType?,
    multisampled: kotlin.Boolean,
    storageTextureFormat: TextureFormat?
) {

    actual constructor(binding: Long, visibility: Long, type: BindingType) : this(
        binding, visibility, type, false, null, null, false, null
    )

    actual constructor(
        binding: Long, visibility: Long, type: BindingType, multisampled: kotlin.Boolean
    ) : this(binding, visibility, type, false, null, null, multisampled, null)

    actual constructor(
        binding: Long,
        visibility: Long,
        type: BindingType,
        multisampled: kotlin.Boolean,
        dimension: TextureViewDimension,
        textureComponentType: TextureComponentType
    ) : this(binding, visibility, type, false, dimension, textureComponentType, multisampled, null)
}

actual class BindGroupLayout internal constructor(val id: Long) {

    override fun toString(): String {
        return "BindGroupLayout$id"
    }
}

actual class PipelineLayoutDescriptor internal constructor(val ids: LongArray) {

    actual constructor(vararg bindGroupLayouts: BindGroupLayout) : this(bindGroupLayouts.map { it.id }.toLongArray())
}

actual class PipelineLayout(val id: Id) {

    override fun toString(): String {
        return "PipelineLayout$id"
    }
}

actual class RenderPipeline internal constructor(val id: Id) {

    override fun toString(): String {
        return "RenderPipeline$id"
    }
}

actual class ComputePipeline internal constructor(val id: Long) {

    override fun toString(): String {
        return "ComputePipeline$id"
    }
}

actual class BlendComponent
actual constructor(
    val srcFactor: BlendFactor, val dstFactor: BlendFactor, val operation: BlendOperation
)

actual class Extent3D actual constructor(width: Long, height: Long, depth: Long) {

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

actual class TextureViewDescriptor
actual constructor(
    format: TextureFormat,
    dimension: TextureViewDimension,
    aspect: TextureAspect,
    baseMipLevel: Long,
    mipLevelCount: Long,
    baseArrayLayer: Long,
    arrayLayerCount: Long
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

actual class TextureView(val id: Long) : IntoBindingResource {

    actual fun destroy() {
        TODO()

    }

    override fun intoBindingResource() {
        TODO()
    }

    override fun toString(): String {
        return "TextureView$id"
    }
}

actual class SwapChainDescriptor
actual constructor(val device: Device, val format: TextureFormat, val usage: Long)

actual class SwapChain(val id: Long, private val window: Window) {

    private val size = window.windowSize

    override fun toString(): String {
        return "SwapChain$id"
    }

    actual fun getCurrentTextureView(): TextureView {
        TODO()
    }

    actual fun present() {
        TODO()
    }

    actual fun isOutOfDate(): Boolean {
        return window.windowSize != size
    }
}

actual class RenderPassColorAttachmentDescriptor
actual constructor(
    attachment: TextureView, clearColor: Color?, resolveTarget: TextureView?, storeOp: StoreOp
) {
}

actual class RenderPassDescriptor
actual constructor(vararg colorAttachments: RenderPassColorAttachmentDescriptor) {
}

actual class CommandBuffer(val id: Long) {

    override fun toString(): String {
        return "CommandBuffer$id"
    }
}

actual class Queue(val id: Long) {

    override fun toString(): String {
        return "Queue$id"
    }

    actual fun submit(vararg cmdBuffers: CommandBuffer) {
        TODO()
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

    override fun intoBindingResource() {
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
        TODO("mapReadAsync not implemented in KGPU.")
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

actual class BindGroupLayoutDescriptor actual constructor(vararg entries: BindGroupLayoutEntry) {

}

actual class BindGroupEntry actual constructor(binding: Long, resource: IntoBindingResource) {

}

actual class BindGroupDescriptor
actual constructor(layout: BindGroupLayout, vararg entries: BindGroupEntry) {
}

actual class BindGroup(val id: Long) {

    override fun toString(): String {
        return "BindGroup$id"
    }
}

actual interface IntoBindingResource {

    fun intoBindingResource()
}

actual class Origin3D actual constructor(x: Long, y: Long, z: Long) {

}

actual class TextureCopyView
actual constructor(texture: Texture, mipLevel: Long, origin: Origin3D) {
}

actual class BufferCopyView
actual constructor(buffer: Buffer, bytesPerRow: Int, rowsPerImage: Int, offset: Long) {

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
) {
}

actual class Sampler(val id: Long) : IntoBindingResource {

    override fun intoBindingResource() {
        TODO()
    }

    override fun toString(): String {
        return "Sampler$id"
    }
}

actual class ComputePipelineDescriptor
actual constructor(layout: PipelineLayout, computeStage: ProgrammableStageDescriptor) {
}

actual class FragmentState actual constructor(
    val module: ShaderModule,
    val entryPoint: String,
    val targets: Array<ColorTargetState>
)

actual class BlendState actual constructor(val color: BlendComponent, val alpha: BlendComponent)

actual class ColorTargetState actual constructor(
    val format: TextureFormat,
    val blendState: BlendState?,
    val writeMask: Long
)

actual class MultisampleState actual constructor(
    val count: Int,
    val mask: Int,
    val alphaToCoverageEnabled: Boolean
)

actual class RenderPipelineDescriptor actual constructor(
    val layout: PipelineLayout,
    val vertexStage: VertexState,
    val primitiveTopology: PrimitiveState,
    val depthStencilState: Any?,
    val multisampleState: MultisampleState,
    val fragmentStage: FragmentState?
)

actual class VertexState actual constructor(
    val module: ShaderModule,
    val entryPoint: String,
    vararg val buffers: VertexBufferLayout
)

actual class PrimitiveState actual constructor(
    val topology: PrimitiveTopology,
    val stripIndexFormat: IndexFormat?,
    val frontFace: FrontFace,
    val cullMode: CullMode
)

actual class VertexAttribute actual constructor(
    val format: VertexFormat,
    val offset: Long,
    val shaderLocation: Int
)

actual class VertexBufferLayout actual constructor(
    val arrayStride: Long,
    val stepMode: InputStepMode,
    vararg attributes: VertexAttribute
)