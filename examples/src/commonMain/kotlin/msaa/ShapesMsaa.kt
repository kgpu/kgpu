package msaa

import io.github.kgpu.*

const val SAMPLE_COUNT = 4;

suspend fun runMsaaTriangle(window: Window) {
    val adapter = Kgpu.requestAdapterAsync(window)
    val device = adapter.requestDeviceAsync();
    val vertexShader = ShaderUtils.fromInternalTextFile(device, "triangle.vert", ShaderType.VERTEX)
    val fragShader = ShaderUtils.fromInternalTextFile(device, "shared.frag", ShaderType.FRAGMENT)

    val vertices = floatArrayOf(
        -.5f, .5f, 0f, 1f, 0f, 0f,
        .5f, .5f, 0f, 0f, 1f, 0f,
        0f, -.5f, 0f, 0f, 0f, 1f
    )
    val buffer = BufferUtils.createFloatBuffer(device, "vertices", vertices, BufferUsage.VERTEX)
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor())

    val pipelineDesc = createRenderPipeline(pipelineLayout, vertexShader, fragShader, CullMode.NONE)
    val pipeline = device.createRenderPipeline(pipelineDesc)
    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM);

    var swapChain = window.configureSwapChain(swapChainDescriptor)
    var texture = createAttachmentTexture(device, window.windowSize)
    var textureView = texture.createView()
    window.onResize = { size : WindowSize ->
        texture.destroy()
        textureView.destroy()
        texture = createAttachmentTexture(device, size)
        textureView = texture.createView()
        swapChain = window.configureSwapChain(swapChainDescriptor)
    }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView();
        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(textureView, Color.WHITE, swapChainTexture)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setVertexBuffer(0, buffer)
        renderPassEncoder.draw(3, 1)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(cmdBuffer)
        swapChain.present();
    }
}

private fun createAttachmentTexture(
    device: Device,
    windowSize: WindowSize
): Texture {
    return device.createTexture(
        TextureDescriptor(
            Extent3D(windowSize.width.toLong(), windowSize.height.toLong(), 1),
            1,
            SAMPLE_COUNT,
            TextureDimension.D2,
            TextureFormat.BGRA8_UNORM,
            TextureUsage.OUTPUT_ATTACHMENT
        )
    )
}

private fun createRenderPipeline(
    pipelineLayout: PipelineLayout,
    vertexModule: ShaderModule,
    fragModule: ShaderModule,
    cullMode: CullMode
): RenderPipelineDescriptor {
    return RenderPipelineDescriptor(
        pipelineLayout,
        ProgrammableStageDescriptor(vertexModule, "main"),
        ProgrammableStageDescriptor(fragModule, "main"),
        PrimitiveTopology.TRIANGLE_LIST,
        RasterizationStateDescriptor(
            FrontFace.CCW,
            cullMode
        ),
        arrayOf(
            ColorStateDescriptor(
                TextureFormat.BGRA8_UNORM,
                BlendDescriptor(),
                BlendDescriptor(),
                0xF
            )
        ),
        Kgpu.undefined,
        VertexStateDescriptor(
            IndexFormat.UINT16, VertexBufferLayoutDescriptor(
                6 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttributeDescriptor(VertexFormat.FLOAT3, 0, 0),
                VertexAttributeDescriptor(VertexFormat.FLOAT3, 3 * Primitives.FLOAT_BYTES, 1)
            )
        ),
        SAMPLE_COUNT,
        0xFFFFFFFF,
        false
    )
}
