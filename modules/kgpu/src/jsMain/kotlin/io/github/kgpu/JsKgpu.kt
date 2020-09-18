package io.github.kgpu

import io.github.kgpu.internal.ArrayBufferUtils
import kotlinx.coroutines.await
import org.khronos.webgl.*
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Promise
import kotlin.browser.document as jsDocument
import kotlin.browser.window as jsWindow

actual object Kgpu {
    actual val backendName: String = "Web"
    actual val undefined = kotlin.js.undefined

    actual fun runLoop(window: Window, func: () -> Unit) {
        window.update()
        func()

        jsWindow.requestAnimationFrame {
            runLoop(window, func)
        };
    }

    actual suspend fun requestAdapterAsync(window: Window?): Adapter {
        return Adapter((js("navigator.gpu.requestAdapter()") as Promise<GPUAdapter>).await())
    }
}

open external class GPUObjectBase {
    val label: String
}

open external class GPUObjectDescriptorBase {
    val label: String
}


actual class Adapter(val jsType: GPUAdapter) {

    actual suspend fun requestDeviceAsync(): Device {
        return Device(jsType.requestDevice().await())
    }

    override fun toString(): String {
        return "Adapter($jsType)"
    }

}

open external class GPUAdapter {
    val name: String
    val extensions: List<GPUExtensionName>

    fun requestDevice(): Promise<GPUDevice>
}

/**
 * Eventually will be external once implemented in browsers
 */
enum class GPUExtensionName {
    TextureCompressionBC,
    PipelineStatisticsQuery,
    TimestampQuery,
    DepthClamping
}

actual enum class PowerPreference(jsType: GPUPowerPreference?) {
    LOW_POWER(GPUPowerPreference.LOW_POWER),
    DEFAULT(null),
    HIGH_PERFORMANCE(GPUPowerPreference.HIGH_PERFORMANCE)
}

/**
 * Eventually will be external once implemented in browsers
 */
enum class GPUPowerPreference {
    LOW_POWER, HIGH_PERFORMANCE
}

actual class Device(val jsType: GPUDevice) {

    override fun toString(): String {
        return "Device($jsType)"
    }

    actual fun createShaderModule(data: ByteArray): ShaderModule {
        val desc = asDynamic()
        val bytes = Int8Array(data.toTypedArray())
        desc.code = Uint32Array(bytes.buffer, 0, data.size / 4)

        return jsType.createShaderModule(desc)
    }

    actual fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline {
        return jsType.createRenderPipeline(desc)
    }

    actual fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout {
        return jsType.createPipelineLayout(desc)
    }

    actual fun createTexture(desc: TextureDescriptor): Texture {
        return Texture(jsType.createTexture(desc))
    }

    actual fun createCommandEncoder(): CommandEncoder {
        return CommandEncoder(jsType.createCommandEncoder())
    }

    actual fun getDefaultQueue(): Queue {
        val queue = jsType.asDynamic().defaultQueue as GPUQueue;

        return Queue(queue)
    }

    actual fun createBuffer(desc: BufferDescriptor): Buffer {
        return Buffer(jsType.createBuffer(desc), desc.size)
    }

    actual fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout {
        return BindGroupLayout(jsType.createBindGroupLayout(desc))
    }

    actual fun createBufferWithData(desc: BufferDescriptor, data: ByteArray): Buffer {
        if(!desc.mappedAtCreation){
            throw IllegalArgumentException("Buffer descriptor must be mapped at creation!")
        }

        val buffer = jsType.createBuffer(desc)
        Uint8Array(buffer.getMappedRange()).set(data.toTypedArray())
        buffer.unmap()

        return Buffer(buffer, desc.size)
    }

    actual fun createBindGroup(desc: BindGroupDescriptor): BindGroup {
        return BindGroup(jsType.createBindGroup(desc))
    }

    actual fun createSampler(desc: SamplerDescriptor): Sampler {
        return Sampler(jsType.createSampler(desc))
    }

    actual fun createComputePipeline(desc: ComputePipelineDescriptor): ComputePipeline {
        return jsType.createComputePipeline(desc)
    }

}

external class GPUDevice {
    val adapter: GPUAdapter
    val extensions: List<GPUExtensionName>
    val limits: Any
    val defaultQueue: Any

    fun createShaderModule(desc: dynamic): GPUShaderModule

    fun createPipelineLayout(desc: PipelineLayoutDescriptor): GPUPipelineLayout

    fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline

    fun createTexture(desc: TextureDescriptor): GPUTexture

    fun createCommandEncoder(): GPUCommandEncoder

    fun createBuffer(desc: BufferDescriptor): GPUBuffer

    fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): GPUBindGroupLayout

    fun createBindGroup(desc: BindGroupDescriptor): GPUBindGroup

    fun createSampler(desc: SamplerDescriptor): GPUSampler

    fun createComputePipeline(desc: ComputePipelineDescriptor): ComputePipeline
}

actual class CommandEncoder(val jsType: GPUCommandEncoder) {

    actual fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder {
        return RenderPassEncoder(jsType.beginRenderPass(desc))
    }

    actual fun finish(): CommandBuffer {
        return CommandBuffer(jsType.finish())
    }

    actual fun copyBufferToTexture(source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D) {
        jsType.copyBufferToTexture(source, destination, copySize)
    }

    actual fun beginComputePass(): ComputePassEncoder {
        return ComputePassEncoder(jsType.beginComputePass())
    }

    actual fun copyBufferToBuffer(
        source: Buffer,
        destination: Buffer,
        size: Long,
        sourceOffset: Int,
        destinationOffset: Int
    ) {
        jsType.copyBufferToBuffer(source.jsType, sourceOffset, destination.jsType, destinationOffset, size)
    }

    actual fun copyTextureToBuffer(source: TextureCopyView, dest: BufferCopyView, size: Extent3D){
        jsType.copyTextureToBuffer(source, dest, size)
    }
}

external class GPUCommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): GPURenderPassEncoder

    fun finish(): GPUCommandBuffer

    fun copyBufferToTexture(source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D)

    fun beginComputePass(): GPUComputePassEncoder

    fun copyBufferToBuffer(
        source: GPUBuffer,
        sourceOffset: Int,
        destination: GPUBuffer,
        destinationOffset: Int,
        size: Long
    )

    fun copyTextureToBuffer(source: TextureCopyView, destination: BufferCopyView, copySize: Extent3D)
}

actual class RenderPassEncoder(val jsType: GPURenderPassEncoder) {
    actual fun setPipeline(pipeline: RenderPipeline) {
        jsType.setPipeline(pipeline)
    }

    actual fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        jsType.draw(vertexCount, instanceCount, firstVertex, firstInstance)
    }

    actual fun endPass() {
        jsType.endPass()
    }

    actual fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long, size: Long) {
        jsType.setVertexBuffer(slot, buffer.jsType, offset, size)
    }

    actual fun drawIndexed(indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int) {
        jsType.drawIndexed(indexCount, instanceCount, firstVertex, baseVertex, firstInstance)
    }

    actual fun setIndexBuffer(buffer: Buffer, offset: Long, size: Long) {
        jsType.setIndexBuffer(buffer.jsType, offset, size)
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        jsType.setBindGroup(index, bindGroup.jsType)
    }
}

external class GPURenderPassEncoder {
    fun setPipeline(pipeline: RenderPipeline)

    fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int)

    fun endPass()

    fun setVertexBuffer(slot: Long, buffer: GPUBuffer, offset: Long, size: Long)

    fun drawIndexed(indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int)

    fun setIndexBuffer(buffer: GPUBuffer, offset: Long, size: Long)

    fun setBindGroup(index: Int, bindGroup: GPUBindGroup)
}

actual class ComputePassEncoder(val jsType: GPUComputePassEncoder) {

    actual fun setPipeline(pipeline: ComputePipeline) {
        jsType.setPipeline(pipeline)
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        jsType.setBindGroup(index, bindGroup.jsType)
    }

    actual fun dispatch(x: Int, y: Int, z: Int) {
        jsType.dispatch(x, y, z)
    }

    actual fun endPass() {
        jsType.endPass()
    }

}

external class GPUComputePassEncoder {
    fun setPipeline(pipeline: ComputePipeline)

    fun setBindGroup(index: Int, bindGroup: GPUBindGroup)

    fun dispatch(x: Int, y: Int, z: Int)

    fun endPass()
}

actual class Texture(val jsType: GPUTexture) {

    actual fun createView(desc: TextureViewDescriptor?): TextureView {
        return TextureView(jsType.createView(desc))
    }

    actual fun destroy(){
        jsType.destroy()
    }
}

external class GPUTexture {

    fun createView(desc: TextureViewDescriptor?): GPUTextureView

    fun destroy()
}

external class GPUShaderModule : GPUObjectBase {
    val compilationInfo: Any
}
actual typealias ShaderModule = GPUShaderModule

actual class ProgrammableStageDescriptor actual constructor(
    val module: ShaderModule,
    val entryPoint: String
)

actual enum class PrimitiveTopology(val jsType: GPUPrimitiveTopology = GPUPrimitiveTopology._NotImplemented) {
    POINT_LIST,
    LINE_LIST,
    LINE_STRIP,
    TRIANGLE_LIST(GPUPrimitiveTopology.triangle_list),
    TRIANGLE_STRIP,
}

enum class GPUPrimitiveTopology {
    point_list,
    line_list,
    line_strip,
    triangle_list,
    triangle_strip,
    _NotImplemented
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

actual class RasterizationStateDescriptor actual constructor(
    frontFace: FrontFace,
    cullMode: CullMode,
    val clampDepth: Boolean,
    val depthBias: Long,
    val depthBiasSlopeScale: Float,
    val depthBiasClamp: Float
) {

    val frontFace = frontFace.jsType
    val cullMode = cullMode.jsType
}

actual class BlendDescriptor actual constructor(
    srcFactor: BlendFactor,
    dstFactor: BlendFactor,
    operation: BlendOperation
) {

    val operation = operation.jsType
    val srcFactor = srcFactor.jsType
    val dstFactor = dstFactor.jsType
}

actual class ColorStateDescriptor actual constructor(
    format: TextureFormat,
    val alphaBlend: BlendDescriptor,
    val colorBlend: BlendDescriptor,
    val writeMask: Long
) {

    val format = format.jsType
}

actual class RenderPipelineDescriptor actual constructor(
    val layout: PipelineLayout,
    val vertexStage: ProgrammableStageDescriptor,
    val fragmentStage: ProgrammableStageDescriptor,
    primitiveTopology: PrimitiveTopology,
    val rasterizationState: RasterizationStateDescriptor,
    val colorStates: Array<ColorStateDescriptor>,
    val depthStencilState: Any?,
    val vertexState: VertexStateDescriptor,
    val sampleCount: Int,
    val sampleMask: Long,
    val alphaToCoverage: Boolean
) {

    val primitiveTopology = "triangle-list"
}

actual class VertexAttributeDescriptor actual constructor(
    format: VertexFormat,
    val offset: Long,
    val shaderLocation: Int
) {
    val format = format.jsType
}

actual class VertexBufferLayoutDescriptor actual constructor(
    val arrayStride: Long,
    stepMode: InputStepMode,
    vararg val attributes: VertexAttributeDescriptor
) {
    val stepMode = stepMode.jsType
}

actual class VertexStateDescriptor actual constructor(
    indexFormat: IndexFormat,
    vararg val vertexBuffers: VertexBufferLayoutDescriptor
) {

    val indexFormat = indexFormat.jsType
}

actual class BindGroupLayoutEntry actual constructor(
    val binding: Long,
    val visibility: Long,
    type: BindingType,
    val hasDynamicOffset: Boolean,
    viewDimension: TextureViewDimension?,
    textureComponentType: TextureComponentType?,
    val multisampled: Boolean,
    storageTextureFormat: TextureFormat?
) {
    val type = type.jsType
    val viewDimension = viewDimension?.jsType ?: undefined
    val textureComponentType = textureComponentType?.jsType ?: undefined
    val storageTextureFormat = storageTextureFormat?.jsType ?: undefined

    actual constructor(binding: Long, visibility: Long, type: BindingType) :
            this(binding, visibility, type, false, null, null, false, null)

    actual constructor(binding: Long, visibility: Long, type: BindingType, multisampled: kotlin.Boolean) :
            this(binding, visibility, type, false, null, null, multisampled, null)


    actual constructor(
        binding: Long,
        visibility: Long,
        type: BindingType,
        multisampled: kotlin.Boolean,
        dimension: TextureViewDimension,
        textureComponentType: TextureComponentType
    ) : this(binding, visibility, type, false, dimension, textureComponentType, multisampled, null)
}

actual class BindGroupLayout(val jsType: GPUBindGroupLayout) {

}

external class GPUBindGroupLayout {

}

actual class PipelineLayoutDescriptor actual constructor(vararg bindGroupLayouts: BindGroupLayout) {
    val bindGroupLayouts = bindGroupLayouts.map { it.jsType }.toTypedArray()
}

external class GPUPipelineLayout
actual typealias PipelineLayout = GPUPipelineLayout

actual class RenderPipeline
actual class ComputePipeline

actual enum class InputStepMode(val jsType: String) {
    VERTEX("vertex"),
    INSTANCE("instance"),
}

actual class Extent3D actual constructor(
    val width: Long,
    val height: Long,
    val depth: Long
)

actual enum class TextureDimension(val jsType: String) {
    D1("1d"),
    D2("2d"),
    D3("3d")
}

actual class TextureDescriptor actual constructor(
    val size: Extent3D,
    val mipLevelCount: Long,
    val sampleCount: Int,
    dimension: TextureDimension,
    format: TextureFormat,
    val usage: Long
) {

    val dimension = dimension.jsType
    val format = format.jsType
}

actual class TextureViewDescriptor actual constructor(
    format: TextureFormat,
    dimension: TextureViewDimension,
    aspect: TextureAspect,
    val baseMipLevel: Long,
    val mipLevelCount: Long,
    val baseArrayLayer: Long,
    val arrayLayerCount: Long
) {

    val format = format.jsType
    val dimension = dimension.jsType
    val aspect = aspect.jsType
}

actual class TextureView(val jsType: GPUTextureView) : IntoBindingResource {

    override fun intoBindingResource(): dynamic {
        return jsType
    }

    actual fun destroy() {
        jsType.destroy()
    }

}

external class GPUTextureView{
    fun destroy()
}

actual class SwapChain(val jsType: GPUSwapChain) {

    actual fun getCurrentTextureView(): TextureView {
        val texture = Texture(jsType.getCurrentTexture())

        return texture.createView(undefined)
    }

    actual fun present() {
        //Not needed on WebGPU
    }

    actual fun isOutOfDate(): Boolean {
        return false
    }
}

external class GPUSwapChain {
    fun getCurrentTexture(): GPUTexture
}

actual class SwapChainDescriptor actual constructor(
    device: Device,
    format: TextureFormat,
    val usage: Long
) {
    val device = device.jsType
    val format = format.jsType
}

actual class RenderPassColorAttachmentDescriptor actual constructor(
    attachment: TextureView,
    clearColor: Color?,
    resolveTarget: TextureView?,
    storeOp: StoreOp
) {
    val attachment = attachment.jsType
    val storeOp = storeOp.jsType
    val loadValue = clearColor ?: LoadOp.LOAD
    val resolveTarget = resolveTarget?.jsType ?: undefined
}

actual class RenderPassDescriptor actual constructor(
    vararg val colorAttachments: RenderPassColorAttachmentDescriptor
)

actual class CommandBuffer(val jsType: GPUCommandBuffer)
external class GPUCommandBuffer

actual class Queue(val jsType: GPUQueue) {

    actual fun submit(vararg cmdBuffers: CommandBuffer) {
        jsType.submit(cmdBuffers.map { it.jsType }.toTypedArray())
    }

    actual fun writeBuffer(buffer: Buffer, data: ByteArray, offset: Long, dataOffset: Long, size: Long) {
        val arrayBuffer = ArrayBuffer(data.size)
        Uint8Array(arrayBuffer).set(data.toTypedArray())

        jsType.writeBuffer(buffer.jsType, offset, arrayBuffer, dataOffset, size)
    }
}

external class GPUQueue {

    fun submit(cmdBuffers: Array<GPUCommandBuffer>)

    fun writeBuffer(buffer: GPUBuffer, offset: Long, data: ArrayBuffer, dataOffset: Long, size: Long)
}

actual class BufferDescriptor actual constructor(
    val label: String,
    val size: Long,
    val usage: Long,
    val mappedAtCreation: Boolean
)

actual class Buffer(val jsType: GPUBuffer, actual val size: Long) : IntoBindingResource {

    actual fun getMappedData(start: Long, size: Long): BufferData {
        return BufferData(Uint8Array(jsType.getMappedRange()))
    }

    actual fun unmap() {
        jsType.unmap()
    }

    override fun intoBindingResource(): dynamic {
        val binding = asDynamic()
        binding.buffer = jsType
        binding.offset = 0
        binding.size = size

        return binding
    }

    actual fun destroy() {
        jsType.destroy()
    }

    actual suspend fun mapReadAsync(device: Device): BufferData {
        jsType.mapAsync(GPUMapMode.READ).await()
        val data = jsType.getMappedRange()

        return BufferData(Uint8Array(data))
    }
}

external object GPUMapMode {
    val READ: Long
    val WRITE: Long
}

external class GPUBuffer {

    fun mapAsync(mode: Long): Promise<dynamic>

    fun getMappedRange() : ArrayBuffer

    fun unmap()

    fun destroy()
}


actual class BufferData(val data: Uint8Array) {

    actual fun putBytes(bytes: ByteArray, offset: Int) {
//        data.set(bytes.toTypedArray(), offset)

        TODO("Unsupported on the Web!")
    }

    actual fun getBytes(): ByteArray {
        return ArrayBufferUtils.toByteArray(data.buffer)
    }

}

actual class BindGroupLayoutDescriptor actual constructor(vararg val entries: BindGroupLayoutEntry) {
}

actual class BindGroupEntry actual constructor(val binding: Long, resource: IntoBindingResource) {

    val resource = resource.intoBindingResource()

}

actual class BindGroupDescriptor actual constructor(
    layout: BindGroupLayout,
    vararg val entries: BindGroupEntry
) {
    val layout = layout.jsType
}

actual class BindGroup(val jsType: GPUBindGroup)

external class GPUBindGroup

actual interface IntoBindingResource {

    fun intoBindingResource(): dynamic

}

actual class Origin3D actual constructor(val x: Long, val y: Long, val z: Long)

actual class TextureCopyView actual constructor(
    texture: Texture,
    val mipLevel: Long,
    val origin: Origin3D
) {
    val texture = texture.jsType
}

actual class BufferCopyView actual constructor(
    buffer: Buffer,
    val bytesPerRow: Int,
    val rowsPerImage: Int,
    val offset: Long
) {

    val buffer = buffer.jsType

}

actual class SamplerDescriptor actual constructor(
    compare: CompareFunction?,
    addressModeU: AddressMode,
    addressModeV: AddressMode,
    addressModeW: AddressMode,
    magFilter: FilterMode,
    minFilter: FilterMode,
    mipmapFilter: FilterMode,
    val lodMinClamp: Float,
    val lodMaxClamp: Float,
    val maxAnisotrophy: Short
) {

    // compare not needed on web because its not a comparison sampler
    val compare = compare?.jsType ?: undefined
    val addressModeU = addressModeU.jsType
    val addressModeV = addressModeV.jsType
    val addressModeW = addressModeW.jsType
    val magFilter = magFilter.jsType
    val minFilter = minFilter.jsType
    val mipmapFilter = mipmapFilter.jsType

}

actual class Sampler(val jsType: GPUSampler) : IntoBindingResource {

    override fun intoBindingResource(): dynamic {
        return jsType
    }

}

external class GPUSampler

actual class ComputePipelineDescriptor actual constructor(
    val layout: PipelineLayout,
    val computeStage: ProgrammableStageDescriptor
)

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
    FLOAT("float"),
    FLOAT2("float2"),
    FLOAT3("float3"),
    FLOAT4("float4"),
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

actual enum class BindingType(val jsType: String) {
    UNIFORM_BUFFER("uniform-buffer"),
    STORAGE_BUFFER("storage-buffer"),
    READONLY_STORAGE_BUFFER("readonly-storage-buffer"),
    SAMPLER("sampler"),
    COMPARISON_SAMPLER("comparison-sampler"),
    SAMPLED_TEXTURE("sampled-texture"),
    READONLY_STORAGE_TEXTURE("readonly-storage-texture"),
    WRITEONLY_STORAGE_TEXTURE("writeonly-storage-texture"),
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

actual enum class TextureComponentType(val jsType: String) {
    FLOAT("float"),
    SINT("sint"),
    UINT("uint")
}