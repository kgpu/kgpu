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

object ShaderVisibility {
    const val VERTEX: Long = 1
    const val FRAGMENT: Long = 2
    const val COMPUTE: Long = 4
}

expect class ShaderModule

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

expect class SwapChain {

    fun getCurrentTextureView(): TextureView

    fun present()

    @Deprecated("Old API. Instead use Window#onResize")
    fun isOutOfDate(): Boolean
}

expect class SwapChainDescriptor(
    device: Device, format: TextureFormat, usage: Long = TextureUsage.OUTPUT_ATTACHMENT
)

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

expect class Texture {
    fun createView(desc: TextureViewDescriptor? = null): TextureView

    fun destroy()
}

expect class TextureView : IntoBindingResource

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

expect class Sampler : IntoBindingResource

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