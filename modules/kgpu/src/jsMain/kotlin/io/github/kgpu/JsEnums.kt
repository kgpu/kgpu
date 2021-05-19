package io.github.kgpu

actual enum class TextureFormat(val jsType: String) {
    R8_UNORM("r8unorm"),
    R8_SNORM("r8snorm"),
    R8_UINT("r8uint"),
    R8_SINT("r8sint"),
    R16_UINT("r16uint"),
    R16_SINT("r16sint"),
    R16_FLOAT("r16float"),
    RG8_UNORM("rg8unorm"),
    RG8_SNORM("rg8snorm"),
    RG8_UINT("rg8uint"),
    RG8_SINT("rg8sint"),
    R32_UINT("r32uint"),
    R32_SINT("r32sint"),
    R32_FLOAT("r32float"),
    RG16_UINT("rg16uint"),
    RG16_SINT("rg16sint"),
    RG16_FLOAT("rg16float"),
    RGBA8_UNORM("rgba8unorm"),
    RGBA8_UNORM_SRGB("rgba8unorm-srgb"),
    RGBA8_SNORM("rgbasnorm"),
    RGBA8_UINT("rgba8uint"),
    RGBA8_SINT("rgba8sint"),
    BGRA8_UNORM("bgra8unorm"),
    BGRA8_UNORM_SRGB("bgra8unorm-srgb"),
    RGB10A2_UNORM("rgb10a2unorm"),
    RG11B10_FLOAT("rg11b10float"),
    RG32_UINT("rg32uint"),
    RG32_SINT("rg32sint"),
    RG32_FLOAT("rg32float"),
    RGBA16_UINT("rgba16uint"),
    RGBA16_SINT("rgba16sint"),
    RGBA16_FLOAT("rgba16float"),
    RGBA32_UINT("rgba32uint"),
    RGBA32_SINT("rgba32sint"),
    RGBA32_FLOAT("rgba32float"),
    DEPTH32_FLOAT("depth32float"),
    DEPTH24_PLUS("depth24plus"),
    DEPTH24_PLUS_STENCIL8("depth32plus-stencil8"),
}

actual enum class BlendOperation(val jsType: String) {
    ADD("add"),
    SUBTRACT("subtract"),
    REVERSE_SUBTRACT("reverse-subtract"),
    MIN("min"),
    MAX("max"),
}

actual enum class StencilOperation(val jsType: String) {
    KEEP("keep"),
    ZERO("zero"),
    REPLACE("replace"),
    INVERT("invert"),
    INCREMENT_CLAMP("increment-clamp"),
    DECREMENT_CLAMP("decrement-clamp"),
    INCREMENT_WRAP("increment-wrap"),
    DECREMENT_WRAP("decrement-wrap"),
}

actual enum class BlendFactor(val jsType: String) {
    ZERO("zero"),
    ONE("one"),
    SRC_COLOR("src-color"),
    ONE_MINUS_SRC_COLOR("one-minus-src-color"),
    SRC_ALPHA("src-alpha"),
    ONE_MINUS_SRC_ALPHA("one-minus-src-alpha"),
    DST_COLOR("dst-color"),
    ONE_MINUS_DST_COLOR("one-minus-dst-color"),
    DST_ALPHA("dst-alpha"),
    ONE_MINUS_DST_ALPHA("one-minus-dst-alpha"),
    SRC_ALPHA_SATURATED("src-alpha-saturated"),
    BLEND_COLOR("blend-color"),
    ONE_MINUS_BLEND_COLOR("one-minus-blend-color"),
}

actual enum class IndexFormat(val jsType: String) {
    UINT16("uint16"),
    UINT32("uint32"),
}

actual enum class VertexFormat(val jsType: String) {
    UCHAR2("uchar2"),
    UCHAR4("uchar4"),
    CHAR2("char2"),
    CHAR4("char4"),
    UCHAR2_NORM("uchar2norm"),
    UCHAR4_NORM("uchar4norm"),
    CHAR2_NORM("char2norm"),
    CHAR4_NORM("char4norm"),
    USHORT2("ushort2"),
    USHORT4("ushort4"),
    SHORT2("short2"),
    SHORT4("short4"),
    USHORT2_NORM("ushort2norm"),
    USHORT4_NORM("ushort4norm"),
    SHORT2_NORM("short2norm"),
    SHORT4_NORM("short4norm"),
    HALF2("half2"),
    HALF4("half4"),
    FLOAT("float32"),
    FLOAT2("float32x2"),
    FLOAT3("float32x3"),
    FLOAT4("float32x4"),
    UINT("uint"),
    UINT2("uint2"),
    UINT3("uint3"),
    UINT4("uint4"),
    INT("int"),
    INT2("int2"),
    INT3("int3"),
    INT4("int4"),
}

actual enum class TextureAspect(val jsType: String) {
    ALL("all"),
    STENCIL_ONLY("stencil-only"),
    DEPTH_ONLY("depth-only"),
}

actual enum class TextureViewDimension(val jsType: String) {
    D1("1d"),
    D2("2d"),
    D2_ARRAY("2d-array"),
    CUBE("cube"),
    CUBE_ARRAY("cube-array"),
    D3("3d"),
}

actual enum class LoadOp(val jsType: String) {
    CLEAR("clear"),
    LOAD("load"),
}

actual enum class StoreOp(val jsType: String) {
    CLEAR("clear"),
    STORE("store"),
}

actual enum class AddressMode(val jsType: String) {
    CLAMP_TO_EDGE("clamp-to-edge"),
    REPEAT("repeat"),
    MIRROR_REPEAT("mirror-repeat"),
}

actual enum class FilterMode(val jsType: String) {
    NEAREST("nearest"),
    LINEAR("linear"),
}

actual enum class CompareFunction(val jsType: String) {
    UNDEFINED("undefined"),
    NEVER("never"),
    LESS("less"),
    EQUAL("equal"),
    LESS_EQUAL("less-equal"),
    GREATER("greater"),
    NOT_EQUAL("not-equal"),
    GREATER_EQUAL("greater-equal"),
    ALWAYS("always"),
}

actual enum class BufferBindingType(val jsType: String) {
    UNIFORM("uniform"), STORAGE("storage"), READ_ONLY_STORAGE("read-only-storage"),
}

actual enum class PrimitiveTopology(
    val jsType: String
) {
    POINT_LIST("point-list"),
    LINE_LIST("line-list"),
    LINE_STRIP("line-strip"),
    TRIANGLE_LIST("triangle-list"),
    TRIANGLE_STRIP("triangle-strip"),
}

actual enum class FrontFace(val jsType: String) {
    CCW("ccw"),
    CW("cw"),
}

actual enum class CullMode(val jsType: String) {
    NONE("none"),
    FRONT("front"),
    BACK("back"),
}

actual enum class InputStepMode(val jsType: String) {
    VERTEX("vertex"),
    INSTANCE("instance"),
}

actual enum class TextureSampleType(val jsType: String) {
    FLOAT("float"),
    SINT("sint"),
    UINT("uint"),
    DEPTH("depth")
}

actual enum class SamplerBindingType(val jsType: String) {
    FILTERING("filtering"),
    NON_FILTERING("non-filtering"),
    COMPARISON("comparison"),
}
