package io.github.kgpu

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
    D1,
    D2,
    D3
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

    /**
     * Red and green channels. 8 bit integer per channel. `[0, 255]` converted to/from float `[0,
     * 1]` in shader.
     */
    RG8_UNORM,

    /**
     * Red and green channels. 8 bit integer per channel. `[-127, 127]` converted to/from float
     * `[-1, 1]` in shader.
     */
    RG8_SNORM,

    /** Red and green channels. 8 bit integer per channel. Unsigned in shader. */
    RG8_UINT,

    /** Red and green channel s. 8 bit integer per channel. Signed in shader. */
    RG8_SINT,

    /** Red channel only. 32 bit integer per channel. Unsigned in shader. */
    R32_UINT,

    /** Red channel only. 32 bit integer per channel. Signed in shader. */
    R32_SINT,

    /** Red channel only. 32 bit float per channel. Float in shader. */
    R32_FLOAT,

    /** Red and green channels. 16 bit integer per channel. Unsigned in shader. */
    RG16_UINT,

    /** Red and green channels. 16 bit integer per channel. Signed in shader. */
    RG16_SINT,

    /** Red and green channels. 16 bit float per channel. Float in shader. */
    RG16_FLOAT,

    /**
     * Red, green, blue, and alpha channels. 8 bit integer per channel. `[0, 255]` converted to/from
     * float `[0, 1]` in shader.
     */
    RGBA8_UNORM,

    /**
     * Red, green, blue, and alpha channels. 8 bit integer per channel. Srgb-color `[0, 255]`
     * converted to/from linear-color float `[0, 1]` in shader.
     */
    RGBA8_UNORM_SRGB,

    /**
     * Red, green, blue, and alpha channels. 8 bit integer per channel. `[-127, 127]` converted
     * to/from float `[-1, 1]` in shader.
     */
    RGBA8_SNORM,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. Unsigned in shader. */
    RGBA8_UINT,

    /** Red, green, blue, and alpha channels. 8 bit integer per channel. Signed in shader. */
    RGBA8_SINT,

    /**
     * Blue, green, red, and alpha channels. 8 bit integer per channel. `[0, 255]` converted to/from
     * float `[0, 1]` in shader.
     */
    BGRA8_UNORM,

    /**
     * Blue, green, red, and alpha channels. 8 bit integer per channel. Srgb-color `[0, 255]`
     * converted to/from linear-color float `[0, 1]` in shader.
     */
    BGRA8_UNORM_SRGB,

    /**
     * Red, green, blue, and alpha channels. 10 bit integer for RGB channels, 2 bit integer for
     * alpha channel. `[0, 1023]` (`[0, 3]` for alpha) converted to/from float `[0, 1]` in shader.
     */
    RGB10A2_UNORM,

    /**
     * Red, green, and blue channels. 11 bit float with no sign bit for RG channels. 10 bit float
     * with no sign bit for blue channel. Float in shader.
     */
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

    /** Special depth format with at least 24 bit integer depth. */
    DEPTH24_PLUS,

    /**
     * Special depth/stencil format with at least 24 bit integer depth and 8 bits integer stencil.
     */
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
    /// Supported on Web and Desktop
    UINT16,
    /// Not supported on web for KGPU.
    UINT32,
}

expect enum class VertexFormat {
    /** Two unsigned bytes. uvec2 in shaders */
    UCHAR2,

    /** Four unsigned bytes. uvec4 in shaders */
    UCHAR4,

    /** Two signed bytes. ivec2 in shaders */
    CHAR2,

    /** Four signed bytes. ivec4 in shaders */
    CHAR4,

    /** Two unsigned bytes `[0, 255]` converted to floats `[0, 1]`. vec2 in shaders */
    UCHAR2_NORM,

    /** Four unsigned bytes `[0, 255]` converted to floats `[0, 1]`. vec4 in shaders */
    UCHAR4_NORM,

    /** two unsigned bytes converted to float `[-1,1]`. vec2 in shaders */
    CHAR2_NORM,

    /** two unsigned bytes converted to float `[-1,1]`. vec2 in shaders */
    CHAR4_NORM,

    /** two unsigned shorts. uvec2 in shaders */
    USHORT2,

    /** four unsigned shorts. uvec4 in shaders */
    USHORT4,

    /** two signed shorts. ivec2 in shaders */
    SHORT2,

    /** four signed shorts. ivec4 in shaders */
    SHORT4,

    /** two unsigned shorts `[0, 65525]` converted to float `[0, 1]`. vec2 in shaders */
    USHORT2_NORM,

    /** four unsigned shorts `[0, 65525]` converted to float `[0, 1]`. vec4 in shaders */
    USHORT4_NORM,

    /** two signed shorts `[-32767, 32767]` converted to float `[-1, 1]`. vec2 in shaders */
    SHORT2_NORM,

    /** two signed shorts `[-32767, 32767]` converted to float `[-1, 1]`. vec4 in shaders */
    SHORT4_NORM,

    /** two half precision floats. vec2 in shaders */
    HALF2,

    /** four half precision floats. vec4 in shaders */
    HALF4,

    /** one float. float in shaders */
    FLOAT,

    /** two floats. vec2 in shaders */
    FLOAT2,

    /** three floats. vec3 in shaders */
    FLOAT3,

    /** four floats. vec4 in shaders */
    FLOAT4,

    /** one unsigned int. uint in shaders */
    UINT,

    /** two unsigned ints. uvec2 in shaders */
    UINT2,

    /** three unsigned ints. uvec3 in shaders */
    UINT3,

    /** four unsigned ints. uvec4 in shaders */
    UINT4,

    /** one signed int. int in shaders */
    INT,

    /** two signed ints. ivec2 in shaders */
    INT2,

    /** three signed ints. ivec2 in shaders */
    INT3,

    /** four signed ints. ivec2 in shaders */
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

expect enum class BufferBindingType {
    UNIFORM,
    STORAGE,
    READ_ONLY_STORAGE,
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
    NEVER,
    LESS,
    EQUAL,
    LESS_EQUAL,
    GREATER,
    NOT_EQUAL,
    GREATER_EQUAL,
    ALWAYS,
}

expect enum class TextureSampleType {
    FLOAT,
    SINT,
    UINT,
    DEPTH
}

expect enum class SamplerBindingType {
    FILTERING,
    NON_FILTERING,
    COMPARISON,
}
