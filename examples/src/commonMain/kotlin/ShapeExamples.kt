import io.github.kgpu.*

object ShapeShaders {
    const val TRIANGLE =
        """
[[stage(vertex)]]
fn vs_main([[builtin(vertex_index)]] in_vertex_index: u32) -> [[builtin(position)]] vec4<f32> {
    let x: f32 = f32(i32(in_vertex_index) - 1);
    let y: f32 = f32(i32(in_vertex_index & 1u) * 2 - 1);
    return vec4<f32>(x, y, 0.0, 1.0);
}

[[stage(fragment)]]
fn fs_main() -> [[location(0)]] vec4<f32> {
    return vec4<f32>(1.0, 0.0, 0.0, 1.0);
}
    """
}

suspend fun runCubeExample(window: Window) {
//    // spotless:off
//    val vertices = floatArrayOf(
//        -1f, -1f, 1f, 1f, 0f, 0f,
//        1f, -1f, 1f, 1f, 0f, 0f,
//        1f, 1f, 1f, 1f, 0f, 0f,
//        -1f, 1f, 1f, 1f, 0f, 0f,
//
//        -1f, 1f, -1f, 0f, 0f, 1f,
//        1f, 1f, -1f, 0f, 0f, 1f,
//        1f, -1f, -1f, 0f, 0f, 1f,
//        -1f, -1f, -1f, 0f, 0f, 1f,
//
//        1f, -1f, -1f, .25f, .4f, .5f,
//        1f, 1f, -1f, .25f, .4f, .5f,
//        1f, 1f, 1f, .25f, .4f, .5f,
//        1f, -1f, 1f, .25f, .4f, .5f,
//
//        -1f, -1f, 1f, .7f, .7f, .2f,
//        -1f, 1f, 1f, .7f, .7f, .2f,
//        -1f, 1f, -1f, .7f, .7f, .2f,
//        -1f, -1f, -1f, .7f, .7f, .2f,
//
//        1f, 1f, -1f, .1f, .4f, .1f,
//        -1f, 1f, -1f, .1f, .4f, .1f,
//        -1f, 1f, 1f, .1f, .4f, .1f,
//        1f, 1f, 1f, .1f, .4f, .1f,
//
//        1f, -1f, 1f, .9f, .5f, .5f,
//        -1f, -1f, 1f, .9f, .5f, .5f,
//        -1f, -1f, -1f, .9f, .5f, .5f,
//        1f, -1f, -1f, .9f, .5f, .5f
//    )
//
//    val indices = shortArrayOf(
//        0, 1, 2, 2, 3, 0,
//        4, 5, 6, 6, 7, 4,
//        8, 9, 10, 10, 11, 8,
//        12, 13, 14, 14, 15, 12,
//        16, 17, 18, 18, 19, 16,
//        20, 21, 22, 22, 23, 20
//    )
//    //spotless:on
//
//    fun getProjectionMatrix(): Matrix4 {
//        val windowSize = window.windowSize
//        val aspectRatio = windowSize.width.toFloat() / windowSize.height
//
//        return Matrix4().perspective(MathUtils.toRadians(45f), aspectRatio, 1f, 10f)
//    }
//
//    val projMatrix = getProjectionMatrix()
//    val viewMatrix = Matrix4().lookAt(Vec3(3.5f, 3.5f, 3f), Vec3(0f, 0f, 0f), Vec3.UNIT_Y)
//    val transMatrix = projMatrix.mul(viewMatrix)
//
//    val adapter = Kgpu.requestAdapterAsync(window)
//    val device = adapter.requestDeviceAsync()
//
//    val shaderModule = device.createShaderModule(ShapeShaders.CUBE)
//
//    val vertexBuffer =
//        BufferUtils.createFloatBuffer(device, "vertices", vertices, BufferUsage.VERTEX)
//    val indexBuffer = BufferUtils.createShortBuffer(device, "indices", indices, BufferUsage.INDEX)
//    val matrixBuffer =
//        BufferUtils.createFloatBuffer(
//            device,
//            "transformation matrix",
//            transMatrix.toFloats(),
//            BufferUsage.UNIFORM or BufferUsage.COPY_DST)
//
//    val descriptor =
//        BindGroupLayoutDescriptor(
//            BindGroupLayoutEntry(0, ShaderVisibility.VERTEX, BindingType.UNIFORM_BUFFER))
//    val bindGroupLayout = device.createBindGroupLayout(descriptor)
//    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
//    val bindGroup =
//        device.createBindGroup(
//            BindGroupDescriptor(bindGroupLayout, BindGroupEntry(0, matrixBuffer)))
//
//    val pipelineDesc =
//        createRenderPipeline(
//            pipelineLayout, shaderModule, CullMode.BACK, IndexFormat.UINT16)
//    val pipeline = device.createRenderPipeline(pipelineDesc)
//    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM)
//
//    var swapChain = window.configureSwapChain(swapChainDescriptor)
//    window.onResize =
//        { _: WindowSize ->
//            swapChain = window.configureSwapChain(swapChainDescriptor)
//        }
//
//    Kgpu.runLoop(window) {
//        val swapChainTexture = swapChain.getCurrentTextureView()
//        val cmdEncoder = device.createCommandEncoder()
//
//        val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.WHITE)
//        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
//        renderPassEncoder.setPipeline(pipeline)
//        renderPassEncoder.setBindGroup(0, bindGroup)
//        renderPassEncoder.setVertexBuffer(0, vertexBuffer)
//        renderPassEncoder.setIndexBuffer(indexBuffer, IndexFormat.UINT16)
//        renderPassEncoder.drawIndexed(indices.size, 1)
//        renderPassEncoder.endPass()
//
//        val cmdBuffer = cmdEncoder.finish()
//        val queue = device.getDefaultQueue()
//
//        viewMatrix.rotate(0f, .01f, 0f)
//        queue.writeBuffer(matrixBuffer, getProjectionMatrix().mul(viewMatrix).toBytes())
//        queue.submit(cmdBuffer)
//        swapChain.present()
//    }
}

suspend fun runTriangleExample(window: Window) {
    val adapter = Kgpu.requestAdapterAsync(window)
    val swapChainFormat = window.getSwapChainPreferredFormat(adapter)
    val device = adapter.requestDeviceAsync()
    val shaderModule = device.createShaderModule(ShapeShaders.TRIANGLE)

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
        VertexState(shaderModule, "vs_main"),
        PrimitiveState(PrimitiveTopology.TRIANGLE_LIST),
        null,
        MultisampleState(1, 0xFFFFFFF, false),
        FragmentState(
            shaderModule, "fs_main", arrayOf(
                ColorTargetState(
                    swapChainFormat, BlendState(BlendComponent(), BlendComponent()), 0xF
                )
            )
        ),
    )

    val pipeline = device.createRenderPipeline(pipelineDesc)
    val swapChainDescriptor = SwapChainDescriptor(device, swapChainFormat)

    var swapChain = window.configureSwapChain(swapChainDescriptor)
    window.onResize =
        { _: WindowSize ->
            swapChain = window.configureSwapChain(swapChainDescriptor)
        }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView()
        val cmdEncoder = device.createCommandEncoder()

        val colorAttachment =
            RenderPassColorAttachmentDescriptor(swapChainTexture, LoadOp.CLEAR, StoreOp.STORE, Color.WHITE)
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