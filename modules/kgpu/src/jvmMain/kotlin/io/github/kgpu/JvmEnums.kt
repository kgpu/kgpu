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

actual enum class TextureViewDimension(val nativeVal: Int) {
    D1(WGPUTextureViewDimension_1D()),
    D2(WGPUTextureViewDimension_2D()),
    D2_ARRAY(WGPUTextureViewDimension_2DArray()),
    CUBE(WGPUTextureViewDimension_Cube()),
    CUBE_ARRAY(WGPUTextureViewDimension_CubeArray()),
    D3(WGPUTextureViewDimension_3D()),
}

actual enum class TextureAspect(val nativeVal: Int) {
    ALL(WGPUTextureAspect_All()),
    STENCIL_ONLY(WGPUTextureAspect_StencilOnly()),
    DEPTH_ONLY(WGPUTextureAspect_DepthOnly()),
}

actual enum class TextureDimension(val nativeVal: Int) {
    D1(WGPUTextureDimension_1D()),
    D2(WGPUTextureDimension_2D()),
    D3(WGPUTextureDimension_3D())
}

actual enum class TextureFormat(val nativeVal: Int) {
    R8_UNORM(WGPUTextureFormat_R8Unorm()),
    R8_SNORM(WGPUTextureFormat_R8Snorm()),
    R8_UINT(WGPUTextureFormat_R8Uint()),
    R8_SINT(WGPUTextureFormat_R8Sint()),
    R16_UINT(WGPUTextureFormat_R16Uint()),
    R16_SINT(WGPUTextureFormat_R16Sint()),
    R16_FLOAT(WGPUTextureFormat_R16Float()),
    RG8_UNORM(WGPUTextureFormat_RG8Unorm()),
    RG8_SNORM(WGPUTextureFormat_RG8Snorm()),
    RG8_UINT(WGPUTextureFormat_RG8Uint()),
    RG8_SINT(WGPUTextureFormat_RG8Sint()),
    R32_UINT(WGPUTextureFormat_R32Uint()),
    R32_SINT(WGPUTextureFormat_R32Sint()),
    R32_FLOAT(WGPUTextureFormat_R32Float()),
    RG16_UINT(WGPUTextureFormat_RG16Uint()),
    RG16_SINT(WGPUTextureFormat_RG16Sint()),
    RG16_FLOAT(WGPUTextureFormat_RG16Float()),
    RGBA8_UNORM(WGPUTextureFormat_RGBA8Unorm()),
    RGBA8_UNORM_SRGB(WGPUTextureFormat_RGBA8UnormSrgb()),
    RGBA8_SNORM(WGPUTextureFormat_RGBA8Snorm()),
    RGBA8_UINT(WGPUTextureFormat_RGBA8Uint()),
    RGBA8_SINT(WGPUTextureFormat_RGBA8Sint()),
    BGRA8_UNORM(WGPUTextureFormat_BGRA8Unorm()),
    BGRA8_UNORM_SRGB(WGPUTextureFormat_BGRA8UnormSrgb()),
    RGB10A2_UNORM(WGPUTextureFormat_RGB10A2Unorm()),
    RG11B10_FLOAT(WGPUTextureFormat_RG11B10Ufloat()),
    RG32_UINT(WGPUTextureFormat_RG32Uint()),
    RG32_SINT(WGPUTextureFormat_RG32Sint()),
    RG32_FLOAT(WGPUTextureFormat_RG32Float()),
    RGBA16_UINT(WGPUTextureFormat_RGBA16Uint()),
    RGBA16_SINT(WGPUTextureFormat_RGBA16Sint()),
    RGBA16_FLOAT(WGPUTextureFormat_RGBA16Float()),
    RGBA32_UINT(WGPUTextureFormat_RGBA32Uint()),
    RGBA32_SINT(WGPUTextureFormat_RGBA32Sint()),
    RGBA32_FLOAT(WGPUTextureFormat_RGBA32Float()),
    DEPTH32_FLOAT(WGPUTextureFormat_Depth32Float()),
    DEPTH24_PLUS(WGPUTextureFormat_Depth24Plus()),
    DEPTH24_PLUS_STENCIL8(WGPUTextureFormat_Depth24PlusStencil8()),
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

actual enum class VertexFormat(val nativeVal: Int) {
    UINT8x2(WGPUVertexFormat_Uint8x2()),
    UINT8x4(WGPUVertexFormat_Uint8x4()),
    SINT8x2(WGPUVertexFormat_Sint8x2()),
    SINT8x4(WGPUVertexFormat_Sint8x4()),
    UNORM8x2(WGPUVertexFormat_Unorm8x2()),
    UNORM8x4(WGPUVertexFormat_Unorm8x4()),
    SNORM8x2(WGPUVertexFormat_Snorm8x2()),
    SNORM8x4(WGPUVertexFormat_Snorm8x4()),
    UINT16x2(WGPUVertexFormat_Uint16x2()),
    UINT16x4(WGPUVertexFormat_Uint16x4()),
    SINT16x2(WGPUVertexFormat_Sint16x2()),
    SINT16x4(WGPUVertexFormat_Sint16x4()),
    UNORM16x2(WGPUVertexFormat_Unorm16x2()),
    UNORM16x4(WGPUVertexFormat_Unorm16x4()),
    SNORM16x2(WGPUVertexFormat_Snorm16x2()),
    SNORM16x4(WGPUVertexFormat_Snorm16x4()),
    FLOAT16x2(WGPUVertexFormat_Float16x2()),
    FLOAT16x4(WGPUVertexFormat_Float16x4()),
    FLOAT32(WGPUVertexFormat_Float32()),
    FLOAT32x2(WGPUVertexFormat_Float32x2()),
    FLOAT32x3(WGPUVertexFormat_Float32x3()),
    FLOAT32x4(WGPUVertexFormat_Float32x4()),
    UINT32(WGPUVertexFormat_Uint32()),
    UINT32x2(WGPUVertexFormat_Uint32x2()),
    UINT32x3(WGPUVertexFormat_Uint32x3()),
    UINT32x4(WGPUVertexFormat_Uint32x4()),
    SINT32(WGPUVertexFormat_Sint32()),
    SINT32x2(WGPUVertexFormat_Sint32x2()),
    SINT32x3(WGPUVertexFormat_Sint32x3()),
    SINT32x4(WGPUVertexFormat_Sint32x4()),
}

actual enum class InputStepMode(val nativeVal: Int) {
    VERTEX(WGPUInputStepMode_Vertex()),
    INSTANCE(WGPUInputStepMode_Instance()),
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

actual enum class AddressMode(val nativeVal: Int) {
    CLAMP_TO_EDGE(WGPUAddressMode_ClampToEdge()),
    REPEAT(WGPUAddressMode_Repeat()),
    MIRROR_REPEAT(WGPUAddressMode_MirrorRepeat()),
}

actual enum class FilterMode(val nativeVal: Int) {
    NEAREST(WGPUFilterMode_Nearest()),
    LINEAR(WGPUFilterMode_Linear()),
}

actual enum class CompareFunction(val nativeVal: Int) {
    NEVER(WGPUCompareFunction_Never()),
    LESS(WGPUCompareFunction_Less()),
    EQUAL(WGPUCompareFunction_Equal()),
    LESS_EQUAL(WGPUCompareFunction_LessEqual()),
    GREATER(WGPUCompareFunction_Greater()),
    NOT_EQUAL(WGPUCompareFunction_NotEqual()),
    GREATER_EQUAL(WGPUCompareFunction_GreaterEqual()),
    ALWAYS(WGPUCompareFunction_Always()),
}

actual enum class TextureSampleType(val nativeVal: Int) {
    FLOAT(WGPUTextureSampleType_Float()),
    SINT(WGPUTextureSampleType_Sint()),
    UINT(WGPUTextureSampleType_Uint()),
    DEPTH(WGPUTextureSampleType_Depth())
}

actual enum class SamplerBindingType(val nativeVal: Int) {
    FILTERING(WGPUSamplerBindingType_Filtering()),
    NON_FILTERING(WGPUSamplerBindingType_NonFiltering()),
    COMPARISON(WGPUSamplerBindingType_Comparison()),
}