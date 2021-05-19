import io.github.kgpu.*
import io.github.kgpu.kcgmath.Matrix4

const val TEXTURE_SHADER =
    """
struct VertexOutput {
    [[location(0)]] uv: vec2<f32>;
    [[builtin(position)]] position: vec4<f32>;
};
            

[[stage(vertex)]]
fn vs_main(
    [[builtin(vertex_index)]] in_vertex_index: u32,
    [[location(0)]] pos: vec2<f32>,
    [[location(1)]] uvs: vec2<f32>) -> VertexOutput {
    
    var output: VertexOutput;
    output.position = vec4<f32>(pos.x, pos.y, 1.0, 1.0);
    output.uv = uvs;
    
    return output;
}

[[group(0), binding(0)]]
var my_texture: texture_2d<f32>;
[[group(0), binding(1)]]
var my_sampler: sampler;

[[stage(fragment)]]
fn fs_main(in: VertexOutput) -> [[location(0)]] vec4<f32> {
    return textureSample(my_texture, my_sampler, in.uv);
}
"""

suspend fun runTextureExample(window: Window) {
    // spotless:off
    val vertices = floatArrayOf(
        -0.5f, -0.5f, 1f, 1f,
        -0.5f, 0.5f, 1f, 0f,
        0.5f, 0.5f, 0f, 0f,
        0.5f, -0.5f, 0f, 1f
    )
    val indices = shortArrayOf(0, 1, 2, 0, 2, 3)
    //spotless:on

    val (image, imageBytes) = loadImage("earth2d.png")

    val adapter = Kgpu.requestAdapterAsync(window)
    val device = adapter.requestDeviceAsync()

    val vertexBuffer =
        BufferUtils.createFloatBuffer(device, "vertices", vertices, BufferUsage.VERTEX)
    val indexBuffer = BufferUtils.createShortBuffer(device, "indices", indices, BufferUsage.INDEX)
    val shader = device.createShaderModule(TEXTURE_SHADER)

    val textureDesc =
        TextureDescriptor(
            Extent3D(image.width.toLong(), image.height.toLong(), 1),
            1,
            1,
            TextureDimension.D2,
            TEXTURE_FORMAT,
            TextureUsage.COPY_DST or TextureUsage.SAMPLED
        )
    val texture = device.createTexture(textureDesc)
    val textureBuffer =
        BufferUtils.createBufferFromData(device, "texture temp", imageBytes, BufferUsage.COPY_SRC)

    var cmdEncoder = device.createCommandEncoder()
    cmdEncoder.copyBufferToTexture(
        BufferCopyView(textureBuffer, image.width * 4, image.height),
        TextureCopyView(texture),
        Extent3D(image.width.toLong(), image.height.toLong(), 1)
    )
    device.getDefaultQueue().submit(cmdEncoder.finish())
    textureBuffer.destroy()

    val sampler = device.createSampler(SamplerDescriptor())
    val textureView = texture.createView()

    val bindGroupLayout =
        device.createBindGroupLayout(
            BindGroupLayoutDescriptor(
                BindGroupLayoutEntry(0, ShaderVisibility.FRAGMENT, TextureBindingLayout()),
                BindGroupLayoutEntry(1, ShaderVisibility.FRAGMENT, SamplerBindingLayout())
            )
        )
    val bindGroup =
        device.createBindGroup(
            BindGroupDescriptor(
                bindGroupLayout,
                BindGroupEntry(0, textureView),
                BindGroupEntry(1, sampler),
            )
        )

    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val pipelineDesc = RenderPipelineDescriptor(
        pipelineLayout,
        VertexState(
            shader, "vs_main",
            VertexBufferLayout(
                4 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttribute(VertexFormat.FLOAT32x2, 0, 0),
                VertexAttribute(VertexFormat.FLOAT32x2, 2 * Primitives.FLOAT_BYTES, 1)
            )
        ),
        PrimitiveState(PrimitiveTopology.TRIANGLE_LIST),
        null,
        MultisampleState(1, 0xFFFFFFF, false),
        FragmentState(
            shader, "fs_main", arrayOf(
                ColorTargetState(
                    TextureFormat.BGRA8_UNORM, BlendState(BlendComponent(), BlendComponent()), 0xF
                )
            )
        ),
    )
    val pipeline = device.createRenderPipeline(pipelineDesc)
    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM)

    var swapChain = window.configureSwapChain(swapChainDescriptor)
    window.onResize = { _ -> swapChain = window.configureSwapChain(swapChainDescriptor) }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView()
        cmdEncoder = device.createCommandEncoder()

        val colorAttachment =
            RenderPassColorAttachmentDescriptor(swapChainTexture, LoadOp.CLEAR, StoreOp.STORE, Color.WHITE)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setBindGroup(0, bindGroup)
        renderPassEncoder.setVertexBuffer(0, vertexBuffer)
        renderPassEncoder.setIndexBuffer(indexBuffer, IndexFormat.UINT16)
        renderPassEncoder.drawIndexed(indices.size, 1)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(cmdBuffer)
        swapChain.present()
    }
}
