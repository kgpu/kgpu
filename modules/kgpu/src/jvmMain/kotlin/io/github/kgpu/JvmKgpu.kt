package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h
import io.github.kgpu.wgpuj.wgpu_h.*
import jdk.incubator.foreign.CLinker
import jdk.incubator.foreign.MemorySegment
import java.nio.ByteOrder
import jdk.incubator.foreign.MemoryHandles
import jdk.incubator.foreign.MemoryAddress
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong

object Platform {
    val isWindows = System.getProperty("os.name").contains("Windows")
    val isLinux = System.getProperty("os.name").contains("Linux")
    val isMac = System.getProperty("os.name").contains("Mac")
}

object CUtils {
    val NULL: MemoryAddress = wgpu_h.NULL()!!
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
            val levelStr = when(level){
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

        if (tracePath != null){
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
        TODO()

    }

    actual fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout {
        TODO()

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
        TODO()
    }

    actual fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout {
        TODO()

    }

    actual fun createBufferWithData(desc: BufferDescriptor, data: ByteArray): Buffer {
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
actual constructor(module: ShaderModule, entryPoint: kotlin.String) {
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

actual class RasterizationStateDescriptor
actual constructor(
    frontFace: FrontFace,
    cullMode: CullMode,
    clampDepth: kotlin.Boolean,
    depthBias: Long,
    depthBiasSlopeScale: kotlin.Float,
    depthBiasClamp: kotlin.Float
) {
}

actual class ColorStateDescriptor
actual constructor(
    format: TextureFormat,
    alphaBlend: BlendDescriptor,
    colorBlend: BlendDescriptor,
    writeMask: Long
) {
}

actual class RenderPipelineDescriptor
actual constructor(
    layout: PipelineLayout,
    vertexStage: ProgrammableStageDescriptor,
    fragmentStage: ProgrammableStageDescriptor,
    primitiveTopology: PrimitiveTopology,
    rasterizationState: RasterizationStateDescriptor,
    colorStates: Array<ColorStateDescriptor>,
    depthStencilState: Any?,
    vertexState: VertexStateDescriptor,
    sampleCount: Int,
    sampleMask: Long,
    alphaToCoverage: kotlin.Boolean
) {
}

actual class VertexAttributeDescriptor
actual constructor(format: VertexFormat, offset: Long, shaderLocation: Int) {
}

actual class VertexBufferLayoutDescriptor
actual constructor(
    arrayStride: Long, stepMode: InputStepMode, vararg attributes: VertexAttributeDescriptor
) {
}

actual class VertexStateDescriptor
actual constructor(
    indexFormat: IndexFormat?, vararg vertexBuffers: VertexBufferLayoutDescriptor
) {
}

actual class BindGroupLayout internal constructor(val id: Long) {

    override fun toString(): String {
        return "BindGroupLayout$id"
    }
}

actual class PipelineLayoutDescriptor actual constructor(vararg bindGroupLayouts: BindGroupLayout) {

}

actual class PipelineLayout(val id: Long) {

    override fun toString(): String {
        return "PipelineLayout$id"
    }
}

actual class RenderPipeline internal constructor(val id: Long) {

    override fun toString(): String {
        return "RenderPipeline$id"
    }
}

actual class ComputePipeline internal constructor(val id: Long) {

    override fun toString(): String {
        return "ComputePipeline$id"
    }
}

actual class BlendDescriptor
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
    label: kotlin.String, size: Long, usage: Long, mappedAtCreation: kotlin.Boolean
) {
}

actual class Buffer(val id: Long, actual val size: Long) : IntoBindingResource {

    override fun intoBindingResource() {
        TODO()
    }

    override fun toString(): String {
        return "Buffer$id"
    }

    actual fun getMappedData(start: Long, size: Long): BufferData {
        TODO()
    }

    actual fun unmap() {
        TODO()
    }

    actual fun destroy() {
        TODO()
    }

    actual suspend fun mapReadAsync(device: Device): BufferData {
        TODO()
    }
}

actual class BufferData(val data: Byte, val size: Int) {

    actual fun putBytes(bytes: ByteArray, offset: Int) {
        TODO()
    }

    actual fun getBytes(): ByteArray {
        TODO()
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
