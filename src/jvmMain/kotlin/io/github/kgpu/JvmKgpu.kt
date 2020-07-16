package io.github.kgpu

import com.noahcharlton.wgpuj.WgpuJava
import com.noahcharlton.wgpuj.jni.*
import com.noahcharlton.wgpuj.util.Platform
import com.noahcharlton.wgpuj.util.SharedLibraryLoader
import io.github.kgpu.GlfwHandler.getOsWindowHandle
import jnr.ffi.Pointer
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWNativeWin32
import org.lwjgl.glfw.GLFWNativeX11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicLong


actual object Kgpu {
    actual val backendName: String = "Desktop"
    actual val undefined = null

    actual fun init() {
        val libraryFile = SharedLibraryLoader().load("wgpu_native")
        WgpuJava.init(libraryFile)

        GlfwHandler.glfwInit()

        println("Wgpu Version: " + WgpuJava.getWgpuNativeVersion())
        println("LWJGL Version: " + Version.getVersion())
        println("GLFW Version: ${GLFW.GLFW_VERSION_MAJOR}.${GLFW.GLFW_VERSION_MINOR}")
    }

    actual fun runLoop(window: Window, func: () -> Unit) {
        while (!window.isCloseRequested()) {
            window.update()
            func()
        }

        GlfwHandler.terminate();
    }
}

actual class Window actual constructor() {
    private val handle: Long = GLFW.glfwCreateWindow(640, 480, "", MemoryUtil.NULL, MemoryUtil.NULL);
    private val surface: Long

    init {
        val osHandle = getOsWindowHandle(handle)
        surface = if (Platform.isWindows) {
            WgpuJava.wgpuNative.wgpu_create_surface_from_windows_hwnd(WgpuJava.createNullPointer(), osHandle)
        } else if (Platform.isLinux) {
            val display = GLFWNativeX11.glfwGetX11Display()
            WgpuJava.wgpuNative.wgpu_create_surface_from_xlib(display, osHandle)
        } else throw UnsupportedOperationException(
            "Platform not supported. See " +
                    "https://github.com/DevOrc/wgpu-java/issues/4"
        )
    }

    init {
        if (handle == MemoryUtil.NULL)
            throw java.lang.RuntimeException("Failed to create the window!")

        GlfwHandler.centerWindow(handle)
    }

    actual fun setTitle(title: String) {
        GLFW.glfwSetWindowTitle(handle, title)
    }

    actual fun isCloseRequested(): Boolean {
        return GLFW.glfwWindowShouldClose(handle)
    }

    actual fun update() {
        GLFW.glfwPollEvents();
    }

    actual suspend fun requestAdapterAsync(preference: PowerPreference): Adapter {
        val adapter = AtomicLong(0)
        val defaultBackend: Int = (1 shl 1) or (1 shl 2) or (1 shl 3)
        val options = WgpuRequestAdapterOptions.createDirect()
        options.compatibleSurface = surface
        options.powerPreference = preference

        WgpuJava.wgpuNative.wgpu_request_adapter_async(
            options.pointerTo,
            defaultBackend,
            false,
            { received: Long, userData: Pointer? -> adapter.set(received) },
            WgpuJava.createNullPointer()
        )

        return Adapter(adapter.get())
    }

    actual fun getWindowSize(): WindowSize {
        val dimension = GlfwHandler.getWindowDimension(handle)

        return WindowSize(dimension.width, dimension.height)
    }

    actual fun configureSwapChain(desc: SwapChainDescriptor): SwapChain {
        val size = getWindowSize()
        val nativeDesc = WgpuSwapChainDescriptor.createDirect();
        nativeDesc.format = desc.format
        nativeDesc.usage = desc.usage
        nativeDesc.presentMode = WgpuPresentMode.FIFO
        nativeDesc.width = size.width.toLong()
        nativeDesc.height = size.height.toLong()

        val id = WgpuJava.wgpuNative.wgpu_device_create_swap_chain(desc.device.id, surface, nativeDesc.pointerTo)

        return SwapChain(id, this)
    }
}

private object GlfwHandler {

    fun glfwInit() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit()) {
            throw RuntimeException("Failed to initialize GLFW!")
        }

        //Do not use opengl
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
    }

    fun centerWindow(handle: Long) {
        val currentDimension = getWindowDimension(handle);
        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

        // Center the window
        GLFW.glfwSetWindowPos(
            handle,
            ((vidmode!!.width() - currentDimension.getWidth()) / 2).toInt(),
            ((vidmode.height() - currentDimension.getHeight()) / 2).toInt()
        )
    }

    fun getWindowDimension(handle: Long): Dimension {
        MemoryStack.stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(handle, width, height)

            return Dimension(width.get(), height.get())
        }
    }

    fun terminate() {
        GLFW.glfwTerminate()

        val callback = GLFW.glfwSetErrorCallback(null)
        callback?.free()
    }

    fun getOsWindowHandle(handle: Long): Long {
        return when {
            Platform.isWindows -> {
                GLFWNativeWin32.glfwGetWin32Window(handle)
            }
            Platform.isLinux -> {
                GLFWNativeX11.glfwGetX11Window(handle)
            }
            else -> {
                throw UnsupportedOperationException(
                    "Platform not supported. See https://github.com/DevOrc/wgpu-java/issues/4"
                )
            }
        }
    }
}

actual class Adapter(val id: Long) {

    override fun toString(): String {
        return "Adapter${Id.fromLong(id)}"
    }

    actual suspend fun requestDeviceAsync(): Device {
        val limits = WgpuCLimits.createDirect();
        limits.maxBindGroups = 4;
        val deviceId = WgpuJava.wgpuNative.wgpu_adapter_request_device(
            id, 0,
            limits.pointerTo, WgpuJava.createNullPointer()
        );

        return Device(deviceId)
    }
}

actual typealias PowerPreference = WgpuPowerPreference

actual class Device(val id: Long) {

    override fun toString(): String {
        return "Device${Id.fromLong(id)}"
    }

    actual fun createShaderModule(data: ByteArray): ShaderModule {
        val desc = WgpuShaderModuleDescriptor.createDirect();
        val codePtr = WgpuJava.createByteArrayPointer(data)
        desc.code.bytes = codePtr;
        desc.code.length = data.size.toLong() / 4 //length is in terms of u32s

        val module = WgpuJava.wgpuNative.wgpu_device_create_shader_module(id, desc.pointerTo)

        return ShaderModule(module)
    }

    actual fun createRenderPipeline(desc: RenderPipelineDescriptor): RenderPipeline {
        val id = WgpuJava.wgpuNative.wgpu_device_create_render_pipeline(id, desc.pointerTo);

        return RenderPipeline(id)
    }

    actual fun createPipelineLayout(desc: PipelineLayoutDescriptor): PipelineLayout {
        val id = WgpuJava.wgpuNative.wgpu_device_create_pipeline_layout(id, desc.pointerTo)

        return PipelineLayout(id)
    }

    actual fun createTexture(desc: TextureDescriptor): Texture {
        val id = WgpuJava.wgpuNative.wgpu_device_create_texture(id, desc.pointerTo)

        return Texture(id)
    }

    actual fun createCommandEncoder(): CommandEncoder {
        val desc = WgpuCommandEncoderDescriptor.createDirect();
        desc.label = "Default Command Encoder";

        val id = WgpuJava.wgpuNative.wgpu_device_create_command_encoder(id, desc.pointerTo)
        return CommandEncoder(id)
    }

    actual fun getDefaultQueue(): Queue {
        val queueId = WgpuJava.wgpuNative.wgpu_device_get_default_queue(id)

        return Queue(queueId)
    }

    actual fun createBuffer(desc: BufferDescriptor): Buffer {
        val id = WgpuJava.wgpuNative.wgpu_device_create_buffer(id, desc.pointerTo)

        return Buffer(id, desc.size)
    }

    actual fun createBindGroupLayout(desc: BindGroupLayoutDescriptor): BindGroupLayout {
        val id = WgpuJava.wgpuNative.wgpu_device_create_bind_group_layout(id, desc.pointerTo)

        return BindGroupLayout(id)
    }

    actual fun createBufferWithData(desc: BufferDescriptor, data: ByteArray): Buffer {
        val buffer = createBuffer(desc)

        buffer.getMappedData(0, data.size.toLong()).putBytes(data, 0)
        buffer.unmap()

        return buffer
    }

    actual fun createBindGroup(desc: BindGroupDescriptor): BindGroup {
        val id = WgpuJava.wgpuNative.wgpu_device_create_bind_group(id, desc.pointerTo)

        return BindGroup(id)
    }

    actual fun createSampler(desc: SamplerDescriptor): Sampler {
        val id = WgpuJava.wgpuNative.wgpu_device_create_sampler(id, desc.pointerTo)

        return Sampler(id)
    }
}

actual class CommandEncoder(val id: Long) {

    override fun toString(): String {
        return "CommandEncoder${Id.fromLong(id)}"
    }

    actual fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder {
        val pass = WgpuJava.wgpuNative.wgpu_command_encoder_begin_render_pass(id, desc.pointerTo);

        return RenderPassEncoder(pass)
    }

    actual fun finish(): CommandBuffer {
        val id = WgpuJava.wgpuNative.wgpu_command_encoder_finish(id, WgpuJava.createNullPointer())

        return CommandBuffer(id)
    }

    actual fun copyBufferToTexture(source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D) {
        WgpuJava.wgpuNative.wgpu_command_encoder_copy_buffer_to_texture(
            id, source.pointerTo, destination.pointerTo, copySize.pointerTo
        )
    }
}


actual class RenderPassEncoder(val pass: WgpuRawPass) {

    override fun toString(): String {
        return "RenderPassEncoder"
    }

    actual fun setPipeline(pipeline: RenderPipeline) {
        WgpuJava.wgpuNative.wgpu_render_pass_set_pipeline(pass.pointerTo, pipeline.id)
    }

    actual fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        WgpuJava.wgpuNative.wgpu_render_pass_draw(
            pass.pointerTo,
            vertexCount, instanceCount, firstVertex, firstInstance
        )
    }

    actual fun endPass() {
        WgpuJava.wgpuNative.wgpu_render_pass_end_pass(pass.pointerTo)
    }

    actual fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long, size: Long) {
        WgpuJava.wgpuNative.wgpu_render_pass_set_vertex_buffer(pass.pointerTo, slot.toInt(), buffer.id, offset, size)
    }

    actual fun drawIndexed(indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int) {
        WgpuJava.wgpuNative.wgpu_render_pass_draw_indexed(
            pass.pointerTo, indexCount, instanceCount,
            firstVertex, baseVertex, firstInstance
        )
    }

    actual fun setIndexBuffer(buffer: Buffer, offset: Long, size: Long) {
        WgpuJava.wgpuNative.wgpu_render_pass_set_index_buffer(pass.pointerTo, buffer.id, offset, size)
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        WgpuJava.wgpuNative.wgpu_render_pass_set_bind_group(
            pass.pointerTo, index, bindGroup.id,
            WgpuJava.createNullPointer(), 0
        )
    }

}

actual class ShaderModule(val moduleId: Long) {

    override fun toString(): String {
        return "ShaderModule${Id.fromLong(moduleId)}"
    }

}

actual class ProgrammableStageDescriptor actual
constructor(module: ShaderModule, entryPoint: kotlin.String) : WgpuProgrammableStageDescriptor(true) {

    init {
        this.entryPoint = entryPoint
        this.module = module.moduleId
    }

}

actual class BindGroupLayoutEntry actual constructor(
    binding: Long,
    visibility: Long,
    type: BindingType,
    hasDynamicOffset: kotlin.Boolean,
    viewDimension: TextureViewDimension?,
    textureComponentType: TextureComponentType?,
    multisampled: kotlin.Boolean,
    storageTextureFormat: TextureFormat?
) : WgpuBindGroupLayoutEntry(true) {

    init {
        this.binding = binding
        this.visibility = visibility
        this.ty = type
        this.hasDynamicOffset = hasDynamicOffset
        this.viewDimension = viewDimension ?: WgpuTextureViewDimension.values()[0]
        this.textureComponentType = textureComponentType ?: WgpuTextureComponentType.values()[0]
        this.multisampled = multisampled
        this.storageTextureFormat = storageTextureFormat ?: WgpuTextureFormat.values()[0]
    }

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

actual typealias PrimitiveTopology = WgpuPrimitiveTopology
actual typealias FrontFace = WgpuFrontFace
actual typealias CullMode = WgpuCullMode
actual typealias TextureFormat = WgpuTextureFormat
actual typealias BlendFactor = WgpuBlendFactor
actual typealias StencilOperation = WgpuStencilOperation
actual typealias BlendOperation = WgpuBlendOperation
actual typealias IndexFormat = WgpuIndexFormat
actual typealias VertexFormat = WgpuVertexFormat
actual typealias InputStepMode = WgpuInputStepMode
actual typealias TextureDimension = WgpuTextureDimension
actual typealias TextureAspect = WgpuTextureAspect
actual typealias TextureViewDimension = WgpuTextureViewDimension
actual typealias LoadOp = WgpuLoadOp
actual typealias StoreOp = WgpuStoreOp
actual typealias BindingType = WgpuBindingType
actual typealias AddressMode = WgpuAddressMode
actual typealias FilterMode = WgpuFilterMode
actual typealias CompareFunction = WgpuCompareFunction
actual typealias TextureComponentType = WgpuTextureComponentType

actual class RasterizationStateDescriptor actual constructor(
    frontFace: FrontFace,
    cullMode: CullMode,
    clampDepth: kotlin.Boolean,
    depthBias: Long,
    depthBiasSlopeScale: kotlin.Float,
    depthBiasClamp: kotlin.Float
) : WgpuRasterizationStateDescriptor(true) {

    init {
        this.frontFace = frontFace
        this.cullMode = cullMode
        this.depthBias = depthBias.toInt()
        this.depthBiasSlopeScale = depthBiasSlopeScale
        this.depthBiasClamp = depthBiasClamp
    }
}

actual class ColorStateDescriptor actual constructor(
    format: TextureFormat,
    alphaBlend: BlendDescriptor,
    colorBlend: BlendDescriptor,
    writeMask: Long
) : WgpuColorStateDescriptor(true) {

    init {
        this.format = format;
        this.writeMask = writeMask

        toWgpuBlend(alphaBlend, this.alphaBlend)
        toWgpuBlend(colorBlend, this.colorBlend)
    }

    companion object {
        private fun toWgpuBlend(src: BlendDescriptor, dst: WgpuBlendDescriptor) {
            dst.dstFactor = src.dstFactor
            dst.operation = src.operation
            dst.srcFactor = src.srcFactor
        }
    }

}

actual class RenderPipelineDescriptor actual constructor(
    layout: PipelineLayout,
    vertexStage: ProgrammableStageDescriptor,
    fragmentStage: ProgrammableStageDescriptor,
    primitiveTopology: PrimitiveTopology,
    rasterizationState: RasterizationStateDescriptor,
    colorStates: Array<ColorStateDescriptor>,
    depthStencilState: Any?,
    vertexState: VertexStateDescriptor,
    sampleCount: Int,
    sampleMask: Long,
    alphaToCoverage: kotlin.Boolean
) : WgpuRenderPipelineDescriptor(true) {

    init {
        this.layout = layout.id;
        this.vertexStage.entryPoint = vertexStage.entryPoint
        this.vertexStage.module = vertexStage.module
        this.fragmentStage.set(fragmentStage)
        this.primitiveTopology = primitiveTopology
        this.rasterizationState.set(rasterizationState)
        this.colorStates.set(colorStates)
        this.colorStatesLength = colorStates.size.toLong()
        this.depthStencilState.set(depthStencilState as WgpuDepthStencilStateDescriptor?)
        this.vertexState.indexFormat = vertexState.indexFormat
        this.vertexState.vertexBuffers.set(vertexState.vertexBuffers.get(vertexState.vertexBuffersLength.toInt()))
        this.vertexState.vertexBuffersLength = vertexState.vertexBuffersLength
        this.sampleCount = sampleCount.toLong()
        this.sampleMask = sampleMask
        this.alphaToCoverageEnabled = alphaToCoverage
    }

}

actual class VertexAttributeDescriptor actual constructor(
    format: VertexFormat,
    offset: Long,
    shaderLocation: Int
) : WgpuVertexAttributeDescriptor(true) {

    init {
        this.format = format
        this.offset = offset
        this.shaderLocation = shaderLocation.toLong()
    }

}

actual class VertexBufferLayoutDescriptor actual constructor(
    arrayStride: Long,
    stepMode: InputStepMode,
    vararg attributes: VertexAttributeDescriptor
) : WgpuVertexBufferLayoutDescriptor(true) {

    init {
        this.arrayStride = arrayStride
        this.stepMode = stepMode
        this.attributes.set(attributes)
        this.attributesLength = attributes.size.toLong()
    }

}

actual class VertexStateDescriptor actual constructor(
    indexFormat: IndexFormat,
    vararg vertexBuffers: VertexBufferLayoutDescriptor
) : WgpuVertexStateDescriptor(true) {
    init {
        this.indexFormat = indexFormat;
        this.vertexBuffers.set(vertexBuffers)
        this.vertexBuffersLength = vertexBuffers.size.toLong()
    }
}

actual class BindGroupLayout internal constructor(val id: Long) {

    override fun toString(): String {
        return "BindGroupLayout${Id.fromLong(id)}"
    }

}

actual class PipelineLayoutDescriptor actual constructor(
    vararg bindGroupLayouts: BindGroupLayout
) : WgpuPipelineLayoutDescriptor(true) {

    init {
        val ids = bindGroupLayouts.map { it.id }.toLongArray()

        this.bindGroupLayouts = WgpuJava.createLongArrayPointer(ids)
        this.bindGroupLayoutsLength = ids.size.toLong()
    }

}

actual class PipelineLayout(val id: Long) {

    override fun toString(): String {
        return "PipelineLayout${Id.fromLong(id)}"
    }

}

actual class RenderPipeline internal constructor(val id: Long) {

    override fun toString(): String {
        return "RenderPipeline${Id.fromLong(id)}"
    }

}

actual class BlendDescriptor actual constructor(
    val srcFactor: BlendFactor,
    val dstFactor: BlendFactor,
    val operation: BlendOperation
)

actual class Extent3D actual constructor(
    width: Long,
    height: Long,
    depth: Long
) : WgpuExtent3d(true) {

    init {
        this.width = width
        this.height = height
        this.depth = depth
    }

}

actual class TextureDescriptor actual constructor(
    size: Extent3D,
    mipLevelCount: Long,
    sampleCount: Int,
    dimension: TextureDimension,
    format: TextureFormat,
    usage: Long
) : WgpuTextureDescriptor(true) {

    init {
        this.size.width = size.width
        this.size.height = size.height
        this.size.depth = size.depth
        this.dimension = dimension
        this.mipLevelCount = mipLevelCount
        this.sampleCount = sampleCount.toLong();
        this.format = format;
        this.usage = usage;
    }

}

actual class TextureViewDescriptor actual constructor(
    format: TextureFormat,
    dimension: TextureViewDimension,
    aspect: TextureAspect,
    baseMipLevel: Long,
    mipLevelCount: Long,
    baseArrayLayer: Long,
    arrayLayerCount: Long
) : WgpuTextureViewDescriptor(true) {

    init {
        this.format = format
        this.dimension = dimension
        this.aspect = aspect
        this.baseMipLevel = baseMipLevel
        this.levelCount = mipLevelCount
        this.baseArrayLayer = baseArrayLayer
        this.arrayLayerCount = arrayLayerCount
    }

}

actual class Texture(val id: Long) {

    actual fun createView(desc: TextureViewDescriptor?): TextureView {
        val ptr = desc?.pointerTo ?: WgpuJava.createNullPointer()

        return TextureView(WgpuJava.wgpuNative.wgpu_texture_create_view(id, ptr))
    }

    override fun toString(): String {
        return "Texture${Id.fromLong(id)}"
    }

}

actual class TextureView(val id: Long) : IntoBindingResource {

    override fun intoBindingResource(resource: WgpuBindingResource) {
        resource.setTag(WgpuBindingResourceTag.TEXTURE_VIEW)
        resource.data.setTextureViewId(id)
    }

    override fun toString(): String {
        return "TextureView${Id.fromLong(id)}"
    }
}

actual class SwapChainDescriptor actual constructor(
    val device: Device,
    val format: TextureFormat,
    val usage: Long
)

actual class SwapChain(val id: Long, private val window: Window) {

    private val size = window.getWindowSize()

    override fun toString(): String {
        return "SwapChain${Id.fromLong(id)}"
    }

    actual fun getCurrentTextureView(): TextureView {
        val output = WgpuSwapChainOutput.createDirect()
        WgpuJava.wgpuNative.wgpu_swap_chain_get_next_texture_jnr_hack(id, output.pointerTo)

        return TextureView(output.viewId)
    }

    actual fun present() {
        WgpuJava.wgpuNative.wgpu_swap_chain_present(id)
    }

    actual fun isOutOfDate(): Boolean {
        return window.getWindowSize() != size
    }
}

actual class RenderPassColorAttachmentDescriptor actual constructor(
    attachment: TextureView,
    clearColor: Color?,
    storeOp: StoreOp
) : WgpuRenderPassColorDescriptor(true) {
    init {
        this.attachment = attachment.id
        this.storeOp = storeOp
        this.loadOp = if (clearColor == null) {
            LoadOp.LOAD
        } else {
            LoadOp.CLEAR
        }

        copyToNativeColor(this.clearColor, clearColor ?: Color.CLEAR)
    }
}

fun copyToNativeColor(native: WgpuColor, color: Color) {
    native.r = color.r
    native.g = color.g
    native.b = color.b
    native.a = color.a
}

actual class RenderPassDescriptor actual constructor(
    vararg colorAttachments: RenderPassColorAttachmentDescriptor
) : WgpuRenderPassDescriptor(
    null,
    *colorAttachments
)

actual class CommandBuffer(val id: Long) {

    override fun toString(): String {
        return "CommandBuffer(${Id.fromLong(id)}"
    }

}

actual class Queue(val id: Long) {

    override fun toString(): String {
        return "Queue${Id.fromLong(id)}"
    }

    actual fun submit(vararg cmdBuffers: CommandBuffer) {
        val ptr = WgpuJava.createLongArrayPointer(cmdBuffers.map { it.id }.toLongArray())

        WgpuJava.wgpuNative.wgpu_queue_submit(id, ptr, cmdBuffers.size)
    }

    actual fun writeBuffer(buffer: Buffer, data: ByteArray, offset: Long, dataOffset: Long, size: Long) {
        val ptr = WgpuJava.createDirectPointer(size.toInt())!!
        ptr.put(0, data, dataOffset.toInt(), size.toInt())

        WgpuJava.wgpuNative.wgpu_queue_write_buffer(id, buffer.id, offset, ptr, size.toInt())
    }

}

actual class BufferDescriptor actual constructor(
    label: kotlin.String,
    size: Long,
    usage: Long,
    mappedAtCreation: kotlin.Boolean
) : WgpuBufferDescriptor(true) {

    init {
        this.label = label
        this.size = size
        this.usage = usage
        this.mappedAtCreation = mappedAtCreation
    }

}

actual class Buffer(val id: Long, actual val size: Long) : IntoBindingResource {

    override fun intoBindingResource(resource: WgpuBindingResource) {
        resource.setTag(WgpuBindingResourceTag.BUFFER)
        resource.data.binding.buffer = id
        resource.data.binding.size = size
        resource.data.binding.offset = 0
    }

    override fun toString(): String {
        return "Buffer${Id.fromLong(id)}"
    }

    actual fun getMappedData(start: Long, size: Long): BufferData {
        return BufferData(WgpuJava.wgpuNative.wgpu_buffer_get_mapped_range(id, start, size))
    }

    actual fun unmap() {
        WgpuJava.wgpuNative.wgpu_buffer_unmap(id)
    }

    actual fun destroy() {
        WgpuJava.wgpuNative.wgpu_buffer_destroy(id)
    }
}

actual class BufferData(val data: Pointer) {

    actual fun putBytes(bytes: ByteArray, offset: Int) {
        data.put(offset.toLong(), bytes, 0, bytes.size)
    }

}

actual class BindGroupLayoutDescriptor actual constructor(
    vararg entries: BindGroupLayoutEntry
) : WgpuBindGroupLayoutDescriptor(true) {

    init {
        this.entries.set(entries)
        this.entriesLength = entries.size.toLong()
    }

}

actual class BindGroupEntry actual constructor(
    binding: Long,
    resource: IntoBindingResource
) : WgpuBindGroupEntry(true) {

    init {
        this.binding = binding
        resource.intoBindingResource(this.resource)
    }

}

actual class BindGroupDescriptor actual constructor(
    layout: BindGroupLayout,
    vararg entries: BindGroupEntry
) : WgpuBindGroupDescriptor(true) {

    init {
        this.layout = layout.id
        this.entries.set(entries)
        this.entriesLength = entries.size.toLong()
    }
}

actual class BindGroup(val id: Long) {

    override fun toString(): String {
        return "BindGroup${Id.fromLong(id)}"
    }

}

actual interface IntoBindingResource {

    fun intoBindingResource(resource: WgpuBindingResource)

}

actual class Origin3D actual constructor(x: Long, y: Long, z: Long) : WgpuOrigin3d(true) {

    init {
        this.x = x
        this.y = y
        this.z = z
    }

}

actual class TextureCopyView actual constructor(
    texture: Texture,
    mipLevel: Long,
    origin: Origin3D
) : WgpuTextureCopyView(true) {

    init {
        this.texture = texture.id
        this.mipLevel = mipLevel
        this.origin.x = origin.x
        this.origin.y = origin.y
        this.origin.z = origin.z
    }

}

actual class BufferCopyView actual constructor(
    buffer: Buffer,
    bytesPerRow: Int,
    rowsPerImage: Int,
    offset: Long
) : WgpuBufferCopyView(true) {

    init {
        this.buffer = buffer.id
        this.layout.bytesPerRow = bytesPerRow.toLong()
        this.layout.rowsPerImage = rowsPerImage.toLong()
        this.layout.offset = offset
    }

}

actual class SamplerDescriptor actual constructor(
    compare: CompareFunction?,
    addressModeU: AddressMode,
    addressModeV: AddressMode,
    addressModeW: AddressMode,
    magFilter: FilterMode,
    minFilter: FilterMode,
    mipmapFilter: FilterMode,
    lodMinClamp: kotlin.Float,
    lodMaxClamp: kotlin.Float,
    maxAnisotrophy: Short
) : WgpuSamplerDescriptor(true) {

    init {
        this.compare = compare ?: WgpuCompareFunction.values()[0]
        this.addressModeU = addressModeU
        this.addressModeV = addressModeV
        this.addressModeW = addressModeV
        this.magFilter = magFilter
        this.minFilter = minFilter
        this.mipmapFilter = mipmapFilter
        this.lodMinClamp = lodMinClamp
        this.lodMaxClamp = lodMaxClamp
    }
}

actual class Sampler(val id: Long) : IntoBindingResource {

    override fun intoBindingResource(resource: WgpuBindingResource) {
        resource.setTag(WgpuBindingResourceTag.SAMPLER)
        resource.data.setSamplerId(id)
    }

    override fun toString(): String {
        return "Sampler(${Id.fromLong(id)}"
    }

}