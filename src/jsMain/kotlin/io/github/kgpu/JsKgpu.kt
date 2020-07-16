package io.github.kgpu

import io.github.kgpu.internal.GlslangLibrary
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint32Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Promise
import kotlin.browser.document as jsDocument
import kotlin.browser.window as jsWindow

actual object Kgpu {
    actual val backendName: String = "Web"
    actual val undefined = kotlin.js.undefined

    actual fun init() {
        GlslangLibrary.init()
    }

    actual fun runLoop(window: Window, func: () -> Unit) {
        func();

        jsWindow.requestAnimationFrame {
            runLoop(window, func)
        };
    }

}

actual class Window actual constructor() {

    private val canvas = kotlin.browser.document.getElementById("kgpuCanvas") as HTMLCanvasElement
    private val context = canvas.getContext("gpupresent")
    private var canvasHackRan = false

    actual fun setTitle(title: String) {
        jsDocument.title = title
    }

    actual fun isCloseRequested(): Boolean {
        return false
    }

    actual fun update() {

    }

    actual suspend fun requestAdapterAsync(preference: PowerPreference): Adapter {
        return Adapter((js("navigator.gpu.requestAdapter()") as Promise<GPUAdapter>).await())
    }

    actual fun getWindowSize(): WindowSize {
        return WindowSize(canvas.width, canvas.height)
    }

    actual fun configureSwapChain(desc: SwapChainDescriptor): SwapChain {
        if (!canvasHackRan) {
            canvas.width += 1 //Hack to get around chromium not showing canvas unless clicked/resized
            canvasHackRan = true
        }

        return SwapChain(context.asDynamic().configureSwapChain(desc) as GPUSwapChain)
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
        val values = jsType.createBufferMapped(desc)
        val buffer = values[0] as GPUBuffer
        val mapping = Int8Array(values[1] as ArrayBuffer)
        mapping.set(data.toTypedArray(), 0)
        buffer.unmap()

        return Buffer(buffer, desc.size)
    }

    actual fun createBindGroup(desc: BindGroupDescriptor): BindGroup {
        return BindGroup(jsType.createBindGroup(desc))
    }

    actual fun createSampler(desc: SamplerDescriptor): Sampler {
        return Sampler(jsType.createSampler(desc))
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

    @Deprecated(message = "No longer part of the spec, but replacement has not been implemented in browsers!")
    fun createBufferMapped(desc: BufferDescriptor): Array<dynamic>

    fun createBindGroup(desc: BindGroupDescriptor): GPUBindGroup

    fun createSampler(desc: SamplerDescriptor): GPUSampler
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
}

external class GPUCommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): GPURenderPassEncoder

    fun finish(): GPUCommandBuffer

    fun copyBufferToTexture(source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D)
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
        jsType.setBindGroup(0, bindGroup.jsType)
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

actual class Texture(val jsType: GPUTexture) {

    actual fun createView(desc: TextureViewDescriptor?): TextureView {
        return TextureView(jsType.createView(desc))
    }

}

external class GPUTexture {

    fun createView(desc: TextureViewDescriptor?): GPUTextureView

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

actual class TextureView(val jsType: GPUTextureView) : IntoBindingResource{

    override fun intoBindingResource(): dynamic {
        return jsType
    }

}
external class GPUTextureView

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
    storeOp: StoreOp
) {
    val attachment = attachment.jsType
    val storeOp = storeOp.jsType
    val loadValue = clearColor ?: LoadOp.LOAD
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
        val data = jsType.getMappedRange(start, size)

        TODO("Not implemented in browsers yet. Use old function Device.createBufferWithData() instead")
//        return BufferData(Uint8Array(data))
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

}

external class GPUBuffer {

    fun getMappedRange(start: Long, size: Long): ArrayBuffer

    fun unmap();

    fun destroy()
}


actual class BufferData(val data: Uint8Array) {

    actual fun putBytes(bytes: ByteArray, offset: Int) {
        data.set(bytes.toTypedArray(), offset)
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
){
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
){

    // compare not needed on web because its not a comparison sampler
    val compare = compare?.jsType ?: undefined
    val addressModeU = addressModeU.jsType
    val addressModeV = addressModeV.jsType
    val addressModeW = addressModeW.jsType
    val magFilter = magFilter.jsType
    val minFilter = minFilter.jsType
    val mipmapFilter = mipmapFilter.jsType

}

actual class Sampler(val jsType: GPUSampler) : IntoBindingResource{

    override fun intoBindingResource(): dynamic {
        return jsType
    }

}

external class GPUSampler

actual enum class TextureFormat(val jsType: String = "") {
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
    RGBA8_UNORM_SRGB("rgba8unorm-srgb"),
    RGBA8_SNORM,
    RGBA8_UINT,
    RGBA8_SINT,
    BGRA8_UNORM("bgra8unorm"),
    BGRA8_UNORM_SRGB("bgra8unorm-srgb"),
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

actual enum class BlendOperation(val jsType: String = "") {
    ADD("add"),
    SUBTRACT,
    REVERSE_SUBTRACT,
    MIN,
    MAX,
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

actual enum class BlendFactor(val jsType: String = "") {
    ZERO("zero"),
    ONE("one"),
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