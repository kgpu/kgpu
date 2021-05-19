package msaa

import io.github.kgpu.*

const val SAMPLE_COUNT = 8

const val MSAA_SHADER =
    """
struct VertexOutput {
    [[location(0)]] color: vec3<f32>;
    [[builtin(position)]] position: vec4<f32>;
};
            

[[stage(vertex)]]
fn vs_main(
    [[location(0)]] pos: vec3<f32>,
    [[location(1)]] color: vec3<f32>) -> VertexOutput {
    var output: VertexOutput;
    output.position = vec4<f32>(pos, 1.0);
    output.color = color;
    
    return output;
}

[[stage(fragment)]]
fn fs_main(in: VertexOutput) -> [[location(0)]] vec4<f32> {
return vec4<f32>(in.color, 1.0);
}
"""

suspend fun runMsaaTriangle(window: Window) {
    val adapter = Kgpu.requestAdapterAsync(window)
    val device = adapter.requestDeviceAsync()
    val shaderModule = device.createShaderModule(MSAA_SHADER)

    // spotless:off
    val vertices = floatArrayOf(
        -.5f, .5f, 0f, 1f, 0f, 0f,
        .5f, .5f, 0f, 0f, 1f, 0f,
        0f, -.5f, 0f, 0f, 0f, 1f
    )
    //spotless:on
    val buffer = BufferUtils.createFloatBuffer(device, "vertices", vertices, BufferUsage.VERTEX)
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor())
    val pipelineDesc = RenderPipelineDescriptor(
        pipelineLayout,
        VertexState(
            shaderModule, "vs_main",
            VertexBufferLayout(
                6 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttribute(VertexFormat.FLOAT32x3, 0, 0),
                VertexAttribute(VertexFormat.FLOAT32x3, 3 * Primitives.FLOAT_BYTES, 1)
            )
        ),
        PrimitiveState(PrimitiveTopology.TRIANGLE_LIST),
        null,
        MultisampleState(SAMPLE_COUNT, 0xFFFFFFF, false),
        FragmentState(
            shaderModule, "fs_main", arrayOf(
                ColorTargetState(
                    TextureFormat.BGRA8_UNORM, BlendState(BlendComponent(), BlendComponent()), 0xF
                )
            )
        ),
    )
    val pipeline = device.createRenderPipeline(pipelineDesc)
    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM)

    var swapChain = window.configureSwapChain(swapChainDescriptor)
    var multisampleTexture = createAttachmentTexture(device, window.windowSize)
    var multisampleTextureView = multisampleTexture.createView()
    window.onResize =
        { size: WindowSize ->
            multisampleTexture.destroy()
            multisampleTexture = createAttachmentTexture(device, size)
            multisampleTextureView = multisampleTexture.createView()
            swapChain = window.configureSwapChain(swapChainDescriptor)
        }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView()
        val cmdEncoder = device.createCommandEncoder()

        val colorAttachment = RenderPassColorAttachmentDescriptor(
            multisampleTextureView,
            LoadOp.CLEAR,
            StoreOp.STORE,
            Color.WHITE,
            swapChainTexture
        )
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setVertexBuffer(0, buffer)
        renderPassEncoder.draw(3, 1)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(cmdBuffer)
        swapChain.present()
    }
}

private fun createAttachmentTexture(device: Device, windowSize: WindowSize): Texture {
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