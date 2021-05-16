package io.github.kgpu

import io.github.kgpu.internal.ArrayBufferUtils
import kotlin.js.Promise
import kotlinx.browser.window as jsWindow
import kotlinx.coroutines.await
import org.khronos.webgl.*

actual object Kgpu {
    actual val undefined = kotlin.js.undefined

    actual fun runLoop(window: Window, func: () -> Unit) {
        window.update()
        func()

        jsWindow.requestAnimationFrame { runLoop(window, func) }
    }

    actual suspend fun requestAdapterAsync(window: Window?): Adapter {
        return Adapter((js("navigator.gpu.requestAdapter()") as Promise<GPUAdapter>).await())
    }
}

open external class GPUObjectBase {
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

    fun requestDevice(): Promise<GPUDevice>
}

actual class Device(val jsType: GPUDevice) {

    override fun toString(): String {
        return "Device($jsType)"
    }

    actual fun createShaderModule(src: String): ShaderModule {
        val desc = asDynamic()
        desc.code = src

        return jsType.createShaderModule(desc)
    }

    actual fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline {
        console.log("Creating render pipeline", desc)
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
        val queue = jsType.asDynamic().queue as GPUQueue

        return Queue(queue)
    }

    actual fun createBuffer(desc: BufferDescriptor): Buffer {
        return Buffer(jsType.createBuffer(desc), desc.size)
    }

    actual fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout {
        console.log(desc)
        return BindGroupLayout(jsType.createBindGroupLayout(desc))
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

actual typealias ShaderModule = GPUShaderModule

external class GPUShaderModule : GPUObjectBase {
    val compilationInfo: Any
}

actual class Origin3D actual constructor(val x: Long, val y: Long, val z: Long)
actual class Extent3D actual constructor(val width: Long, val height: Long, val depth: Long)

actual enum class TextureDimension(val jsType: String) {
    D1("1d"),
    D2("2d"),
    D3("3d")
}

actual class TextureDescriptor
actual constructor(
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

actual class Texture(val jsType: GPUTexture) {

    actual fun createView(desc: TextureViewDescriptor?): TextureView {
        return TextureView(jsType.createView(desc))
    }

    actual fun destroy() {
        jsType.destroy()
    }
}

external class GPUTexture {

    fun createView(desc: TextureViewDescriptor?): GPUTextureView

    fun destroy()
}

actual class TextureViewDescriptor
actual constructor(
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

external class GPUTextureView {
    fun destroy()
}

actual class SwapChainDescriptor
actual constructor(device: Device, format: TextureFormat, val usage: Long) {
    val device = device.jsType
    val format = format.jsType
}

actual class SwapChain(val jsType: GPUSwapChain) {

    actual fun getCurrentTextureView(): TextureView {
        val texture = Texture(jsType.getCurrentTexture())

        return texture.createView(undefined)
    }

    actual fun present() {
        // Not needed on WebGPU
    }

    actual fun isOutOfDate(): Boolean {
        return false
    }
}

external class GPUSwapChain {
    fun getCurrentTexture(): GPUTexture
}

actual class Queue(val jsType: GPUQueue) {

    actual fun submit(vararg cmdBuffers: CommandBuffer) {
        jsType.submit(cmdBuffers.map { it.jsType }.toTypedArray())
    }

    actual fun writeBuffer(
        buffer: Buffer, data: ByteArray, offset: Long, dataOffset: Long, size: Long
    ) {
        val arrayBuffer = ArrayBuffer(data.size)
        Uint8Array(arrayBuffer).set(data.toTypedArray())

        jsType.writeBuffer(buffer.jsType, offset, arrayBuffer, dataOffset, size)
    }
}

external class GPUQueue {

    fun submit(cmdBuffers: Array<GPUCommandBuffer>)

    fun writeBuffer(
        buffer: GPUBuffer, offset: Long, data: ArrayBuffer, dataOffset: Long, size: Long
    )
}

actual class BufferData(val data: Uint8Array) {

    actual fun putBytes(bytes: ByteArray, offset: Int) {
        data.set(bytes.toTypedArray(), offset)
    }

    actual fun getBytes(): ByteArray {
        return ArrayBufferUtils.toByteArray(data.buffer)
    }
}

external object GPUMapMode {
    val READ: Long
    val WRITE: Long
}

actual class BufferDescriptor actual constructor(
    val label: String,
    val size: Long,
    val usage: Int,
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

external class GPUBuffer {

    fun mapAsync(mode: Long): Promise<dynamic>

    fun getMappedRange(): ArrayBuffer

    fun unmap()

    fun destroy()
}

actual class SamplerDescriptor
actual constructor(
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