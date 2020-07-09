package io.github.kgpu

expect object Kgpu {
    val backendName: String
    val undefined: Nothing?

    fun runLoop(window: Window, func: () -> Unit)
}

expect class Device {

    fun createShaderModule(data: ByteArray): ShaderModule

    fun createRenderPipeline(desc: RenderPipelineDescriptor) : RenderPipeline

    fun createPipelineLayout(desc: PipelineLayoutDescriptor) : PipelineLayout
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

    suspend fun requestAdapterAsync(preference: PowerPreference): Adapter
}

expect class BindGroupLayoutEntry{
    //TODO
}

expect class ShaderModule
expect class ProgrammableStageDescriptor(module: ShaderModule, entryPoint: String)
expect class PipelineLayout
expect class BindGroupLayout
expect class PipelineLayoutDescriptor(bindGroupLayouts: Array<BindGroupLayout>)
expect class RenderPipeline

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
    sampleMask: Int,
    alphaToCoverage: Boolean);


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
        frontFace: FrontFace,
        cullMode: CullMode,
        clampDepth: Boolean,
        depthBias: Long,
        depthBiasSlopeScale: Float,
        depthBiasClamp: Float
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
        stride: Long,
        stepMode: InputStepMode,
        attributes: Array<VertexAttributeDescriptor>
)

expect class VertexStateDescriptor(
        indexFormat: IndexFormat,
        vertexBuffers: Array<VertexBufferLayoutDescriptor>
)

expect class BlendDescriptor(srcFactor: BlendFactor, dstFactor: BlendFactor, operation: BlendOperation)

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