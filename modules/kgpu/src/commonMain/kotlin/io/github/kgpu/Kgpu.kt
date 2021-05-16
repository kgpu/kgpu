package io.github.kgpu

expect object Kgpu {
    /**
     * A cross platform version of undefined. On the JVM, this value is equal to null, and on the
     * web, this is equal to undefined
     */
    val undefined: Nothing?

    /**
     * Runs a loop while the window is open. This loop will automatically update the window while
     * the loop is running. The loop will stop when the window is requested to be closed.
     */
    fun runLoop(window: Window, func: () -> Unit)

    /**
     * Requests an adapter
     *
     * @param window the window to create the adapter for. This parameter is only needed for
     * graphics applications
     */
    suspend fun requestAdapterAsync(window: Window? = null): Adapter
}

object Primitives {
    const val FLOAT_BYTES: Long = 4L
    const val INT_BYTES: Long = 4L
    const val LONG_BYTES: Long = 8L
}

expect class Device {

    fun createShaderModule(src: String): ShaderModule

    fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline

    fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout

    fun createTexture(desc: TextureDescriptor): Texture

    fun createCommandEncoder(): CommandEncoder

    fun getDefaultQueue(): Queue

    fun createBuffer(desc: BufferDescriptor): Buffer

    fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout

    fun createBindGroup(desc: BindGroupDescriptor): BindGroup

    fun createSampler(desc: SamplerDescriptor): Sampler

    fun createComputePipeline(desc: ComputePipelineDescriptor): ComputePipeline
}

expect class Adapter {

    suspend fun requestDeviceAsync(): Device
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
// Tomorrow - fix compute example on JS backend
expect class CommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder

    fun finish(): CommandBuffer

    fun copyBufferToTexture(
        source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D
    )

    fun beginComputePass(): ComputePassEncoder

    fun copyBufferToBuffer(
        source: Buffer,
        destination: Buffer,
        size: Long = destination.size,
        sourceOffset: Int = 0,
        destinationOffset: Int = 0
    )

    fun copyTextureToBuffer(source: TextureCopyView, dest: BufferCopyView, size: Extent3D)
}

expect class RenderPassEncoder {

    fun setPipeline(pipeline: RenderPipeline)

    fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int = 0, firstInstance: Int = 0)

    fun endPass()

    fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long = 0, size: Long = buffer.size)

    fun drawIndexed(
        indexCount: Int,
        instanceCount: Int,
        firstVertex: Int = 0,
        baseVertex: Int = 0,
        firstInstance: Int = 0
    )

    fun setIndexBuffer(
        buffer: Buffer, indexFormat: IndexFormat, offset: Long = 0, size: Long = buffer.size
    )

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
 * Represents something that is a binding resource. Examples include buffer, samplers, and texture
 * views.
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
 * The usages determine what kind of memory the texture is allocated from and what actions the
 * texture can partake in.
 */
object TextureUsage {
    /** Allows a texture to be the source in a copy operation */
    const val COPY_SRC: Long = 1

    /**
     * Allows a texture to the destination of a copy operation such as
     * [CommandEncoder.copyBufferToTexture]
     */
    const val COPY_DST: Long = 2

    /** Allows a texture to be a sampled texture in a bind group */
    const val SAMPLED: Long = 4

    /** Allows a texture to be a storage texture in a bind group */
    const val STORAGE: Long = 8

    /** Allows a texture to be a output attachment of a render pass */
    const val OUTPUT_ATTACHMENT: Long = 16
}

expect class RenderPassColorAttachmentDescriptor(
    attachment: TextureView,
    loadOp: LoadOp,
    storeOp: StoreOp,
    clearColor: Color? = null,
    resolveTarget: TextureView? = null,
)

expect class RenderPassDescriptor(vararg colorAttachments: RenderPassColorAttachmentDescriptor)

expect class SwapChain {

    fun getCurrentTextureView(): TextureView

    fun present()

    @Deprecated("Old API. Instead use Window#onResize")
    fun isOutOfDate(): Boolean
}

expect class BindGroupEntry(binding: Long, resource: IntoBindingResource)

expect class BindGroupDescriptor(layout: BindGroupLayout, vararg entries: BindGroupEntry)

expect class BufferBinding(buffer: Buffer, offset: Long = 0, size: Long = buffer.size - offset) : IntoBindingResource

object ShaderVisibility {

    const val VERTEX: Long = 1
    const val FRAGMENT: Long = 2
    const val COMPUTE: Long = 4
}

expect abstract class BindingLayout()

expect class BufferBindingLayout(
    type: BufferBindingType = BufferBindingType.UNIFORM,
    hasDynamicOffset: Boolean = false,
    minBindingSize: Long = 0,
) : BindingLayout

// TODO: Implement these
//class SamplerBindingLayout() : BindingLayout()
//class TextureBindingLayout() : BindingLayout()
//class StorageTextureBindingLayout() : BindingLayout()
//class ExternalTextureBindingLayout() : BindingLayout()

expect class BindGroupLayoutEntry(
    binding: Long,
    visibility: Long,
    bindingLayout: BindingLayout)

expect class ComputePipelineDescriptor(
    layout: PipelineLayout, computeStage: ProgrammableStageDescriptor
)

expect class BindGroupLayoutDescriptor(vararg entries: BindGroupLayoutEntry)

expect class SwapChainDescriptor(
    device: Device, format: TextureFormat, usage: Long = TextureUsage.OUTPUT_ATTACHMENT
)

expect class Texture {
    fun createView(desc: TextureViewDescriptor? = null): TextureView

    fun destroy()
}

expect class TextureView : IntoBindingResource {

    fun destroy()
}

/**
 * The usages determine what kind of memory the buffer is allocated from and what actions the buffer
 * can partake in.
 */
object BufferUsage {
    /**
     * Allow a buffer to be mapped for reading. Does not need to be enabled for mapped_at_creation.
     */
    const val MAP_READ: Int = 1

    /**
     * Allow a buffer to be mapped for writing. Does not need to be enabled for mapped_at_creation.
     */
    const val MAP_WRITE: Int = 2

    /**
     * Allow a buffer to be the source buffer for [CommandEncoder.copyBufferToBuffer] or
     * [CommandEncoder.copyBufferToTexture]
     */
    const val COPY_SRC: Int = 4

    /** Allow a buffer to be the destination buffer for [CommandEncoder.copyBufferToBuffer] */
    const val COPY_DST: Int = 8

    /** Allow a buffer to be used as index buffer for draw calls */
    const val INDEX: Int = 16

    /** Allow a buffer to be used as vertex buffer for draw calls */
    const val VERTEX: Int = 32

    /** Allow a buffer to be used as uniform buffer */
    const val UNIFORM: Int = 64

    /** Allows a buffer to be used as a storage buffer */
    const val STORAGE: Int = 128

    /** Allow a buffer to be the indirect buffer in an indirect draw call. */
    const val INDIRECT: Int = 256

    const val QUERY_RESOLVE: Int = 512
}

expect class BufferDescriptor(label: String, size: Long, usage: Int, mappedAtCreation: Boolean)

expect class Buffer {

    val size: Long

    /** JVM Only */
    fun getMappedData(start: Long = 0, size: Long = this.size): BufferData

    @Deprecated(
        "Eventually will be replaced with mapAsync() and getMappedData() but waiting on Wgpu-native!"
    )
    suspend fun mapReadAsync(device: Device): BufferData

    fun unmap()

    fun destroy()
}

/**
 * A cross platform representation of mapped buffer data. On the desktop it is backed by a pointer,
 * and on the web it is backed by an ArrayBuffer.
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
    vertex: VertexState,
    primitive: PrimitiveState,
    depthStencil: Any?,
    multisample: MultisampleState,
    fragment: FragmentState?
)

expect class MultisampleState(
    count: Int,
    mask: Int,
    alphaToCoverageEnabled: Boolean,
)

expect class VertexState(
    module: ShaderModule,
    entryPoint: String,
    vararg buffers: VertexBufferLayout
)

expect class PrimitiveState(
    topology: PrimitiveTopology,
    stripIndexFormat: IndexFormat? = null,
    frontFace: FrontFace = FrontFace.CCW,
    cullMode: CullMode = CullMode.NONE,
)

expect class FragmentState(
    module: ShaderModule,
    entryPoint: String,
    targets: Array<ColorTargetState>
)

expect class ColorTargetState(
    format: TextureFormat,
    blendState: BlendState?,
    writeMask: Long
)

expect class BlendState(color: BlendComponent, alpha: BlendComponent)

expect class VertexAttribute(format: VertexFormat, offset: Long, shaderLocation: Int)

expect class VertexBufferLayout(
    arrayStride: Long, stepMode: InputStepMode, vararg attributes: VertexAttribute
)

expect class BlendComponent(
    srcFactor: BlendFactor = BlendFactor.ONE,
    dstFactor: BlendFactor = BlendFactor.ZERO,
    operation: BlendOperation = BlendOperation.ADD
)

expect class TextureCopyView(
    texture: Texture, mipLevel: Long = 0, origin: Origin3D = Origin3D(0, 0, 0)
)

expect class BufferCopyView(
    buffer: Buffer, bytesPerRow: Int, rowsPerImage: Int, offset: Long = 0
) {}

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