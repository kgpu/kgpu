package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h.*;

actual enum class PrimitiveTopology(val nativeVal: Int) {
    POINT_LIST(WGPUPrimitiveTopology_PointList()),
    LINE_LIST(WGPUPrimitiveTopology_LineList()),
    LINE_STRIP(WGPUPrimitiveTopology_LineStrip()),
    TRIANGLE_LIST(WGPUPrimitiveTopology_TriangleList()),
    TRIANGLE_STRIP(WGPUPrimitiveTopology_TriangleStrip()),
}

actual enum class FrontFace(val nativeVal: Int) {
    CCW(WGPUFrontFace_CCW()),
    CW(WGPUFrontFace_CW()),
}

actual enum class CullMode(val nativeVal: Int) {
    NONE(WGPUCullMode_None()),
    FRONT(WGPUCullMode_Front()),
    BACK(WGPUCullMode_Back()),
}

actual enum class TextureViewDimension {
    D1,
    D2,
    D2_ARRAY,
    CUBE,
    CUBE_ARRAY,
    D3,
}

actual enum class TextureAspect {
    ALL,
    STENCIL_ONLY,
    DEPTH_ONLY,
}

actual enum class TextureDimension {
    D1,
    D2,
    D3
}

actual enum class TextureFormat(val nativeVal: Int = Int.MAX_VALUE) {
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
    RGBA8_UNORM(WGPUTextureFormat_RGBA8Unorm()),
    RGBA8_UNORM_SRGB,
    RGBA8_SNORM,
    RGBA8_UINT,
    RGBA8_SINT,
    BGRA8_UNORM(WGPUTextureFormat_BGRA8Unorm()),
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

actual enum class BlendOperation(val nativeVal: Int) {
    ADD(WGPUBlendOperation_Add()),
    SUBTRACT(WGPUBlendOperation_Subtract()),
    REVERSE_SUBTRACT(WGPUBlendOperation_ReverseSubtract()),
    MIN(WGPUBlendOperation_Min()),
    MAX(WGPUBlendOperation_Max()),
}

actual enum class StencilOperation {
    KEEP,
    ZERO,
    REPLACE,
    INVERT,
    INCREMENT_CLAMP,
    DECREMENT_CLAMP,
    INCREMENT_WRAP,
    DECREMENT_WRAP,
}

actual enum class BlendFactor(val nativeVal: Int) {
    ZERO(WGPUBlendFactor_Zero()),
    ONE(WGPUBlendFactor_One()),
    SRC_COLOR(WGPUBlendFactor_SrcColor()),
    ONE_MINUS_SRC_COLOR(WGPUBlendFactor_OneMinusSrcColor()),
    SRC_ALPHA(WGPUBlendFactor_SrcAlpha()),
    ONE_MINUS_SRC_ALPHA(WGPUBlendFactor_OneMinusSrcAlpha()),
    DST_COLOR(WGPUBlendFactor_DstColor()),
    ONE_MINUS_DST_COLOR(WGPUBlendFactor_OneMinusDstColor()),
    DST_ALPHA(WGPUBlendFactor_DstAlpha()),
    ONE_MINUS_DST_ALPHA(WGPUBlendFactor_DstAlpha()),
    SRC_ALPHA_SATURATED(WGPUBlendFactor_SrcAlphaSaturated()),
    BLEND_COLOR(WGPUBlendFactor_BlendColor()),
    ONE_MINUS_BLEND_COLOR(WGPUBlendFactor_OneMinusBlendColor()),
}

actual enum class IndexFormat(val nativeVal: Int) {
    UINT16(WGPUIndexFormat_Uint16()),
    UINT32(WGPUIndexFormat_Uint32()),
}

actual enum class VertexFormat {
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

actual enum class InputStepMode {
    VERTEX,
    INSTANCE,
}

actual enum class LoadOp(val nativeVal: Int) {
    CLEAR(WGPULoadOp_Clear()),
    LOAD(WGPULoadOp_Load()),
}

actual enum class StoreOp(val nativeVal: Int) {
    CLEAR(WGPUStoreOp_Clear()),
    STORE(WGPUStoreOp_Store()),
}

actual enum class BufferBindingType(val nativeVal: Int) {
    UNIFORM(WGPUBufferBindingType_Uniform()),
    STORAGE(WGPUBufferBindingType_Storage()),
    READ_ONLY_STORAGE(WGPUBufferBindingType_ReadOnlyStorage()),
}

actual enum class AddressMode {
    CLAMP_TO_EDGE,
    REPEAT,
    MIRROR_REPEAT,
}

actual enum class FilterMode {
    NEAREST,
    LINEAR,
}

actual enum class CompareFunction {
    NEVER,
    LESS,
    EQUAL,
    LESS_EQUAL,
    GREATER,
    NOT_EQUAL,
    GREATER_EQUAL,
    ALWAYS,
}

actual enum class TextureComponentType {
    FLOAT,
    SINT,
    UINT
}
