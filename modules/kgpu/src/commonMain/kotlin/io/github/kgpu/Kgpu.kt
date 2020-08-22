package io.github.kgpu

expect object Kgpu {
    val backendName: String

    /**
     * A cross platform version of undefined. On the JVM, this value is equal to null,
     * and on the web, this is equal to undefined
     */
    val undefined: Nothing?

    /**
     * Runs a loop while the window is open. This loop will automatically update
     * the window while the loop is running. The loop will stop when the window
     * is requested to be closed.
     */
    fun runLoop(window: Window, func: () -> Unit)

    /**
     * Requests an adapter
     *
     * @param window the window to create the adapter for. This parameter
     * is only needed for graphics applications
     */
    suspend fun requestAdapterAsync(window: Window? = null): Adapter
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

    fun createSampler(desc: SamplerDescriptor): Sampler

    fun createComputePipeline(desc: ComputePipelineDescriptor): ComputePipeline
}

expect class Adapter {

    suspend fun requestDeviceAsync(): Device

}

expect enum class PowerPreference {
    LOW_POWER, DEFAULT, HIGH_PERFORMANCE
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

expect class CommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder

    fun finish(): CommandBuffer

    fun copyBufferToTexture(source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D)

    fun beginComputePass(): ComputePassEncoder

    fun copyBufferToBuffer(
        source: Buffer, destination: Buffer, size: Long = destination.size,
        sourceOffset: Int = 0, destinationOffset: Int = 0
    )
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

expect class ComputePassEncoder {

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

/**
 * Represents something that is a binding resource. Examples
 * include buffer, samplers, and texture views.
 *
 * __See:__ [Binding Resource Spec](https://gpuweb.github.io/gpuweb/#typedefdef-gpubindingresource)
 */
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

/**
 * The usages determine what kind of memory the texture is allocated from and what actions the texture can partake in.
 */
object TextureUsage {
    /** Allows a texture to be the source in a copy operation*/
    const val COPY_SRC: Long = 1

    /** Allows a texture to the destination of a copy operation such as [CommandEncoder.copyBufferToTexture] */
    const val COPY_DST: Long = 2

    /** Allows a texture to be a sampled texture in a bind group*/
    const val SAMPLED: Long = 4

    /** Allows a texture to be a storage texture in a bind group*/
    const val STORAGE: Long = 8

    /** Allows a texture to be a output attachment of a render pass */
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

    @Deprecated("Old API. Instead use Window#onResize")
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
) {
    constructor(
        binding: Long,
        visibility: Long,
        type: BindingType
    )

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

expect class TextureView : IntoBindingResource {

    fun destroy()

}

/**
 * The usages determine what kind of memory the buffer is allocated from and what actions the buffer can partake in.
 */
object BufferUsage {
    /** Allow a buffer to be mapped for reading. Does not need to be enabled for mapped_at_creation. */
    const val MAP_READ: Long = 1

    /** Allow a buffer to be mapped for writing. Does not need to be enabled for mapped_at_creation. */
    const val MAP_WRITE: Long = 2

    /** Allow a buffer to be the source buffer for [CommandEncoder.copyBufferToBuffer] or
     * [CommandEncoder.copyBufferToTexture]  */
    const val COPY_SRC: Long = 4

    /** Allow a buffer to be the destination buffer for [CommandEncoder.copyBufferToBuffer] */
    const val COPY_DST: Long = 8

    /** Allow a buffer to be used as index buffer for draw calls */
    const val INDEX: Long = 16

    /** Allow a buffer to be used as vertex buffer for draw calls */
    const val VERTEX: Long = 32

    /** Allow a buffer to be used as uniform buffer */
    const val UNIFORM: Long = 64

    /** Allows a buffer to be used as a storage buffer */
    const val STORAGE: Long = 128

    /** Allow a buffer to be the indirect buffer in an indirect draw call. */
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

    @Deprecated("Eventually will be replaced with mapAsync() and getMappedData() but waiting on Wgpu-native!")
    suspend fun mapReadAsync(device: Device): BufferData

    fun unmap()

    fun destroy()
}

/**
 * A cross platform representation of mapped buffer data.
 * On the desktop it is backed by a pointer, and on the
 * web it is backed by an ArrayBuffer.
 */
expect class BufferData {

    fun putBytes(bytes: ByteArray, offset: Int = 0)

    fun getBytes(): ByteArray

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
    /** 8 Bit Red channel only. `[0, 255]` converted to/from float `[0, 1]` in shader. */
    R8_UNORM,

    /** 8 Bit Red channel only. `[-127, 127]` converted to/from float `[-1, 1]` in shader. */
    R8_SNORM,

    /** Red channel only. 8 bit integer per channel. Unsigned in shader. */
    R8_UINT,

    /** Red channel only. 8 bit integer per channel. Signed in shader. */
    R8_SINT,

    /** Red channel only. 16 bit integer per channel. Unsigned in shader. */
    R16_UINT,

    /** Red channel only. 16 bit integer per channel. Signed in shader. */
    R16_SINT,

    /** Red channel only. 16 bit float per channel. Float in shader. */
    R16_FLOAT,

    /** Red and green channels. 8 bit integer per channel. `[0, 255]` converted to/from float `[0, 1]` in shader. */
    RG8_UNORM,

    /** Red and green channels. 8 bit integer per channel. `[-127, 127]` converted to/from float `[-1, 1]` in shader. */
    RG8_SNORM,

    /** Red and green channels. 8 bit integer per channel. Unsigned in shader. */
    RG8_UINT,

    /** Red and green channel s. 8 bit integer per channel. Signed in shader. */
    RG8_SINT,

    /** Red channel only. 32 bit integer per channel. Unsigned in shader. */
    R32_UINT,

    /** Red channel only. 32 bit integer per channel. Signed in shader. */
    R32_SINT,

    /** Red channel only. 32 bit float per channel. Float in shader.*/
    R32_FLOAT,

    /** Red and green channels. 16 bit integer per channel. Unsigned in shader. */
    RG16_UINT,

    /** Red and green channels. 16 bit integer per channel. Signed in shader.*/
    RG16_SINT,

    /** Red and green channels. 16 bit float per channel. Float in shader. */
    RG16_FLOAT,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. `[0, 255]` converted to/from float `[0, 1]` in shader. */
    RGBA8_UNORM,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. Srgb-color `[0, 255]` converted to/from linear-color float `[0, 1]` in shader. */
    RGBA8_UNORM_SRGB,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. `[-127, 127]` converted to/from float `[-1, 1]` in shader. */
    RGBA8_SNORM,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. Unsigned in shader. */
    RGBA8_UINT,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. Signed in shader. */
    RGBA8_SINT,

    /** Blue, green, red, and alpha channels. 8 bit integer per channel. `[0, 255]` converted to/from float `[0, 1]` in shader. */
    BGRA8_UNORM,

    /** Blue, green, red, and alpha channels. 8 bit integer per channel. Srgb-color `[0, 255]` converted to/from linear-color float `[0, 1]` in shader. */
    BGRA8_UNORM_SRGB,

    /** Red, green, blue, and alpha channels. 10 bit integer for RGB channels, 2 bit integer for alpha channel. `[0, 1023]` (`[0, 3]` for alpha) converted to/from float `[0, 1]` in shader.*/
    RGB10A2_UNORM,

    /** Red, green, and blue channels. 11 bit float with no sign bit for RG channels. 10 bit float with no sign bit for blue channel. Float in shader. */
    RG11B10_FLOAT,

    /** Red and green channels. 32 bit integer per channel. Unsigned in shader. */
    RG32_UINT,

    /** Red and green channels. 32 bit integer per channel. Signed in shader. */
    RG32_SINT,

    /** Red and green channels. 32 bit float per channel. Float in shader. */
    RG32_FLOAT,

    /** Red, green, blue, and alpha channels. 16 bit integer per channel. Unsigned in shader. */
    RGBA16_UINT,

    /** Red, green, blue, and alpha channels. 16 bit integer per channel. Signed in shader. */
    RGBA16_SINT,

    /** Red, green, blue, and alpha channels. 16 bit float per channel. Float in shader. */
    RGBA16_FLOAT,

    /** Red, green, blue, and alpha channels. 32 bit integer per channel. Unsigned in shader. */
    RGBA32_UINT,

    /** Red, green, blue, and alpha channels. 32 bit integer per channel. Signed in shader. */
    RGBA32_SINT,

    /** Red, green, blue, and alpha channels. 32 bit float per channel. Float in shader. */
    RGBA32_FLOAT,

    /** Special depth format with 32 bit floating point depth. */
    DEPTH32_FLOAT,

    /** Special depth format with at least 24 bit integer depth.*/
    DEPTH24_PLUS,

    /** Special depth/stencil format with at least 24 bit integer depth and 8 bits integer stencil.*/
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
    /** Two unsigned bytes. uvec2 in shaders */
    UCHAR2,

    /** Four unsigned bytes. uvec4 in shaders */
    UCHAR4,

    /**Two signed bytes. ivec2 in shaders*/
    CHAR2,

    /**Four signed bytes. ivec4 in shaders*/
    CHAR4,

    /**Two unsigned bytes `[0, 255]` converted to floats `[0, 1]`. vec2 in shaders*/
    UCHAR2_NORM,

    /**Four unsigned bytes `[0, 255]` converted to floats `[0, 1]`. vec4 in shaders*/
    UCHAR4_NORM,

    /**two unsigned bytes converted to float `[-1,1]`. vec2 in shaders */
    CHAR2_NORM,

    /**two unsigned bytes converted to float `[-1,1]`. vec2 in shaders */
    CHAR4_NORM,

    /**two unsigned shorts. uvec2 in shaders*/
    USHORT2,

    /**four unsigned shorts. uvec4 in shaders*/
    USHORT4,

    /**two signed shorts. ivec2 in shaders*/
    SHORT2,

    /**four signed shorts. ivec4 in shaders */
    SHORT4,

    /** two unsigned shorts `[0, 65525]` converted to float `[0, 1]`. vec2 in shaders*/
    USHORT2_NORM,

    /** four unsigned shorts `[0, 65525]` converted to float `[0, 1]`. vec4 in shaders*/
    USHORT4_NORM,

    /** two signed shorts `[-32767, 32767]` converted to float `[-1, 1]`. vec2 in shaders*/
    SHORT2_NORM,

    /** two signed shorts `[-32767, 32767]` converted to float `[-1, 1]`. vec4 in shaders*/
    SHORT4_NORM,

    /**two half precision floats. vec2 in shaders*/
    HALF2,

    /**four half precision floats. vec4 in shaders*/
    HALF4,

    /**one float. float in shaders*/
    FLOAT,

    /**two floats. vec2 in shaders*/
    FLOAT2,

    /**three floats. vec3 in shaders*/
    FLOAT3,

    /**four floats. vec4 in shaders*/
    FLOAT4,

    /**one unsigned int. uint in shaders*/
    UINT,

    /**two unsigned ints. uvec2 in shaders*/
    UINT2,

    /**three unsigned ints. uvec3 in shaders*/
    UINT3,

    /**four unsigned ints. uvec4 in shaders*/
    UINT4,

    /**one signed int. int in shaders*/
    INT,

    /**two signed ints. ivec2 in shaders*/
    INT2,

    /**three signed ints. ivec2 in shaders*/
    INT3,

    /**four signed ints. ivec2 in shaders*/
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

expect enum class AddressMode {
    CLAMP_TO_EDGE,
    REPEAT,
    MIRROR_REPEAT,
}

expect enum class FilterMode {
    NEAREST,
    LINEAR,
}

expect enum class CompareFunction {
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

expect enum class TextureComponentType {
    FLOAT, SINT, UINT
}