package io.github.kgpu

expect object Kgpu {
    val backendName: String
    val undefined: Nothing?

    fun runLoop(window: Window, func: () -> Unit)

    /**
     * Requests an adapter
     *
     * @param window the window to create the adapter for. This parameter
     * is only needed for graphics applications
     */
    suspend fun requestAdapterAsync(window: Window? = null) : Adapter
}

object Primitives {
    const val FLOAT_BYTES: Long = 4
    const val INT_BYTES: Long = 4
}

expect class Device {

    fun createShaderModule(data: ByteArray): ShaderModule

    fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline

    fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout

    fun createTexture(desc: TextureDescriptor): Texture

    fun createCommandEncoder(): CommandEncoder

    fun getDefaultQueue(): Queue

    fun createBuffer(desc: BufferDescriptor): Buffer

    fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout

    @Deprecated(message = "No longer part of the spec, but replacement has not been implemented in browsers!")
    fun createBufferWithData(desc: BufferDescriptor, data: ByteArray): Buffer

    fun createBindGroup(desc: BindGroupDescriptor): BindGroup

    fun createSampler(desc: SamplerDescriptor) : Sampler

    fun createComputePipeline(desc: ComputePipelineDescriptor) : ComputePipeline
}

expect class Adapter {

    suspend fun requestDeviceAsync(): Device

}

expect enum class PowerPreference {
    LOW_POWER, DEFAULT, HIGH_PERFORMANCE
}

expect class Window() {

    fun setTitle(title: String)

    fun isCloseRequested(): Boolean

    fun update()

    fun getWindowSize(): WindowSize

    fun configureSwapChain(desc: SwapChainDescriptor): SwapChain
}

class Color(val r: Double, val g: Double, val b: Double, val a: Double) {

    companion object {
        val BLACK = Color(0.0, 0.0, 0.0, 1.0)
        val WHITE = Color(1.0, 1.0, 1.0, 1.0)
        val RED = Color(1.0, 0.0, 0.0, 1.0)
        val GREEN = Color(0.0, 1.0, 0.0, 1.0)
        val BLUE = Color(0.0, 0.0, 1.0, 1.0)
        val CLEAR = Color(0.0, 0.0, 0.0, 0.0)
    }

}

data class WindowSize(val width: Int, val height: Int) {
    override fun toString(): String {
        return "WindowSize($width, $height)"
    }
}

expect class CommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder

    fun finish(): CommandBuffer

    fun copyBufferToTexture(source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D)

    fun beginComputePass() : ComputePassEncoder

    fun copyBufferToBuffer(source: Buffer, destination: Buffer, size: Long = destination.size,
                           sourceOffset: Int = 0, destinationOffset: Int = 0)
}

expect class RenderPassEncoder {

    fun setPipeline(pipeline: RenderPipeline)

    fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int = 0, firstInstance: Int = 0)

    fun endPass()

    fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long = 0, size: Long = buffer.size)

    fun drawIndexed(
        indexCount: Int, instanceCount: Int, firstVertex: Int = 0, baseVertex: Int = 0, firstInstance: Int = 0
    )

    fun setIndexBuffer(buffer: Buffer, offset: Long = 0, size: Long = buffer.size)

    fun setBindGroup(index: Int, bindGroup: BindGroup)
}

expect class ComputePassEncoder{

    fun setPipeline(pipeline: ComputePipeline)

    fun setBindGroup(index: Int, bindGroup: BindGroup)

    fun dispatch(x: Int, y: Int = 1, z: Int = 1)

    fun endPass()

}

expect class Queue {

    fun submit(vararg cmdBuffers: CommandBuffer)

    fun writeBuffer(
        buffer: Buffer,
        data: ByteArray,
        offset: Long = 0,
        dataOffset: Long = 0,
        size: Long = data.size.toLong()
    )
}

expect interface IntoBindingResource
expect class ShaderModule
expect class ProgrammableStageDescriptor(module: ShaderModule, entryPoint: String)
expect class PipelineLayout
expect class BindGroupLayout
expect class PipelineLayoutDescriptor(vararg bindGroupLayouts: BindGroupLayout)
expect class RenderPipeline
expect class ComputePipeline
expect class CommandBuffer
expect class BindGroup
expect class Sampler : IntoBindingResource

expect class Extent3D(width: Long, height: Long, depth: Long)
expect class Origin3D(x: Long, y: Long, z: Long)

object TextureUsage {
    const val COPY_SRC: Long = 1
    const val COPY_DST: Long = 2
    const val SAMPLED: Long = 4
    const val STORAGE: Long = 8
    const val OUTPUT_ATTACHMENT: Long = 16
}

expect class RenderPassColorAttachmentDescriptor(
    attachment: TextureView,
    clearColor: Color?,
    resolveTarget: TextureView? = null,
    storeOp: StoreOp = StoreOp.STORE
)

expect class RenderPassDescriptor(
    vararg colorAttachments: RenderPassColorAttachmentDescriptor
)

expect class SwapChain {

    fun getCurrentTextureView(): TextureView

    fun present()

    fun isOutOfDate(): Boolean
}

expect class BindGroupEntry(binding: Long, resource: IntoBindingResource)

expect class BindGroupDescriptor(layout: BindGroupLayout, vararg entries: BindGroupEntry)

object ShaderVisibility {

    const val VERTEX: Long = 1
    const val FRAGMENT: Long = 2
    const val COMPUTE: Long = 4

}

expect class BindGroupLayoutEntry(
    binding: Long,
    visibility: Long,
    type: BindingType,
    hasDynamicOffset: Boolean,
    viewDimension: TextureViewDimension?,
    textureComponentType: TextureComponentType?,
    multisampled: Boolean,
    storageTextureFormat: TextureFormat?
){
    constructor(binding: Long,
                 visibility: Long,
                 type: BindingType)

    constructor(
        binding: Long,
        visibility: Long,
        type: BindingType,
        multisampled: Boolean
    )

    constructor(
        binding: Long,
        visibility: Long,
        type: BindingType,
        multisampled: Boolean,
        dimension: TextureViewDimension,
        textureComponentType: TextureComponentType
    )
}

expect class ComputePipelineDescriptor(layout: PipelineLayout, computeStage: ProgrammableStageDescriptor)

expect class BindGroupLayoutDescriptor(vararg entries: BindGroupLayoutEntry)

expect class SwapChainDescriptor(
    device: Device,
    format: TextureFormat,
    usage: Long = TextureUsage.OUTPUT_ATTACHMENT
)

expect class Texture {
    fun createView(desc: TextureViewDescriptor? = null): TextureView

    fun destroy()
}

expect class TextureView : IntoBindingResource{

    fun destroy()

}

object BufferUsage {
    const val MAP_READ: Long = 1
    const val MAP_WRITE: Long = 2
    const val COPY_SRC: Long = 4
    const val COPY_DST: Long = 8
    const val INDEX: Long = 16
    const val VERTEX: Long = 32
    const val UNIFORM: Long = 64
    const val STORAGE: Long = 128
    const val INDIRECT: Long = 256
    const val QUERY_RESOLVE: Long = 512
}

expect class BufferDescriptor(
    label: String,
    size: Long,
    usage: Long,
    mappedAtCreation: Boolean
)

expect class Buffer : IntoBindingResource {

    val size: Long

    /**
     * JVM Only
     */
    fun getMappedData(start: Long = 0, size: Long = this.size): BufferData

    suspend fun mapReadAsync(device: Device) : BufferData

    fun unmap()

    fun destroy()
}

expect class BufferData {

    fun putBytes(bytes: ByteArray, offset: Int = 0)

    fun getBytes() : ByteArray

}

expect class TextureViewDescriptor(
    format: TextureFormat,
    dimension: TextureViewDimension,
    aspect: TextureAspect = TextureAspect.ALL,
    baseMipLevel: Long = 0,
    mipLevelCount: Long = 0,
    baseArrayLayer: Long = 0,
    arrayLayerCount: Long = 0
)

expect class TextureDescriptor(
    size: Extent3D,
    mipLevelCount: Long,
    sampleCount: Int,
    dimension: TextureDimension,
    format: TextureFormat,
    usage: Long
)

expect class RenderPipelineDescriptor(
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
    alphaToCoverage: Boolean
);


expect enum class PrimitiveTopology {
    POINT_LIST,
    LINE_LIST,
    LINE_STRIP,
    TRIANGLE_LIST,
    TRIANGLE_STRIP,
}

expect enum class FrontFace {
    CCW,
    CW,
}

expect enum class CullMode {
    NONE,
    FRONT,
    BACK,
}

expect class RasterizationStateDescriptor(
    frontFace: FrontFace = FrontFace.CCW,
    cullMode: CullMode = CullMode.NONE,
    clampDepth: Boolean = false,
    depthBias: Long = 0,
    depthBiasSlopeScale: Float = 0f,
    depthBiasClamp: Float = 0f
)

expect class ColorStateDescriptor(
    format: TextureFormat,
    alphaBlend: BlendDescriptor,
    colorBlend: BlendDescriptor,
    writeMask: Long
)

expect class VertexAttributeDescriptor(
    format: VertexFormat,
    offset: Long,
    shaderLocation: Int
)

expect class VertexBufferLayoutDescriptor(
    arrayStride: Long,
    stepMode: InputStepMode,
    vararg attributes: VertexAttributeDescriptor
)

expect class VertexStateDescriptor(
    indexFormat: IndexFormat,
    vararg vertexBuffers: VertexBufferLayoutDescriptor
)

expect class BlendDescriptor(
    srcFactor: BlendFactor = BlendFactor.ONE,
    dstFactor: BlendFactor = BlendFactor.ZERO,
    operation: BlendOperation = BlendOperation.ADD
)

expect class TextureCopyView(
    texture: Texture,
    mipLevel: Long = 0,
    origin: Origin3D = Origin3D(0, 0, 0)
)

expect class BufferCopyView(buffer: Buffer, bytesPerRow: Int, rowsPerImage: Int, offset: Long = 0) {

}

expect class SamplerDescriptor(
    compare: CompareFunction? = null,
    addressModeU: AddressMode = AddressMode.CLAMP_TO_EDGE,
    addressModeV: AddressMode = AddressMode.CLAMP_TO_EDGE,
    addressModeW: AddressMode = AddressMode.CLAMP_TO_EDGE,
    magFilter: FilterMode = FilterMode.NEAREST,
    minFilter: FilterMode = FilterMode.NEAREST,
    mipmapFilter: FilterMode = FilterMode.NEAREST,
    lodMinClamp: Float = 0f,
    lodMaxClamp: Float = 100000000f,
    maxAnisotrophy: Short = 1
)

expect enum class TextureViewDimension {
    D1,
    D2,
    D2_ARRAY,
    CUBE,
    CUBE_ARRAY,
    D3,
}

expect enum class TextureAspect {
    ALL,
    STENCIL_ONLY,
    DEPTH_ONLY,
}

expect enum class TextureDimension {
    D1, D2, D3
}

expect enum class TextureFormat {
    R8_UNORM,
    R8_SNORM,
    R8_UINT,
    R8_SINT,
    R16_UINT,
    R16_SINT,
    R16_FLOAT,
    RG8_UNORM,
    RG8_SNORM,
    RG8_UINT,
    RG8_SINT,
    R32_UINT,
    R32_SINT,
    R32_FLOAT,
    RG16_UINT,
    RG16_SINT,
    RG16_FLOAT,
    RGBA8_UNORM,
    RGBA8_UNORM_SRGB,
    RGBA8_SNORM,
    RGBA8_UINT,
    RGBA8_SINT,
    BGRA8_UNORM,
    BGRA8_UNORM_SRGB,
    RGB10A2_UNORM,
    RG11B10_FLOAT,
    RG32_UINT,
    RG32_SINT,
    RG32_FLOAT,
    RGBA16_UINT,
    RGBA16_SINT,
    RGBA16_FLOAT,
    RGBA32_UINT,
    RGBA32_SINT,
    RGBA32_FLOAT,
    DEPTH32_FLOAT,
    DEPTH24_PLUS,
    DEPTH24_PLUS_STENCIL8,
}

expect enum class BlendOperation {
    ADD,
    SUBTRACT,
    REVERSE_SUBTRACT,
    MIN,
    MAX,
}

expect enum class StencilOperation {
    KEEP,
    ZERO,
    REPLACE,
    INVERT,
    INCREMENT_CLAMP,
    DECREMENT_CLAMP,
    INCREMENT_WRAP,
    DECREMENT_WRAP,
}

expect enum class BlendFactor {
    ZERO,
    ONE,
    SRC_COLOR,
    ONE_MINUS_SRC_COLOR,
    SRC_ALPHA,
    ONE_MINUS_SRC_ALPHA,
    DST_COLOR,
    ONE_MINUS_DST_COLOR,
    DST_ALPHA,
    ONE_MINUS_DST_ALPHA,
    SRC_ALPHA_SATURATED,
    BLEND_COLOR,
    ONE_MINUS_BLEND_COLOR,
}

expect enum class IndexFormat {
    UINT16,
    UINT32,
}

expect enum class VertexFormat {
    UCHAR2,
    UCHAR4,
    CHAR2,
    CHAR4,
    UCHAR2_NORM,
    UCHAR4_NORM,
    CHAR2_NORM,
    CHAR4_NORM,
    USHORT2,
    USHORT4,
    SHORT2,
    SHORT4,
    USHORT2_NORM,
    USHORT4_NORM,
    SHORT2_NORM,
    SHORT4_NORM,
    HALF2,
    HALF4,
    FLOAT,
    FLOAT2,
    FLOAT3,
    FLOAT4,
    UINT,
    UINT2,
    UINT3,
    UINT4,
    INT,
    INT2,
    INT3,
    INT4,
}

expect enum class InputStepMode {
    VERTEX,
    INSTANCE,
}

expect enum class LoadOp {
    CLEAR,
    LOAD,
}

expect enum class StoreOp {
    CLEAR,
    STORE,
}

expect enum class BindingType {
    UNIFORM_BUFFER,
    STORAGE_BUFFER,
    READONLY_STORAGE_BUFFER,
    SAMPLER,
    COMPARISON_SAMPLER,
    SAMPLED_TEXTURE,
    READONLY_STORAGE_TEXTURE,
    WRITEONLY_STORAGE_TEXTURE,
}

expect enum class AddressMode{
    CLAMP_TO_EDGE,
    REPEAT,
    MIRROR_REPEAT,
}

expect enum class FilterMode{
    NEAREST,
    LINEAR,
}

expect enum class CompareFunction{
    UNDEFINED,
    NEVER,
    LESS,
    EQUAL,
    LESS_EQUAL,
    GREATER,
    NOT_EQUAL,
    GREATER_EQUAL,
    ALWAYS,
}

expect enum class TextureComponentType{
    FLOAT, SINT, UINT
}