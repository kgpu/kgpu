import io.github.kgpu.*


private fun mathTest() {
    val matrix = Matrix4f().translate(1f, 2f, 3f)

    println("Op0 = ${matrix.toFloats().joinToString()}")
}

suspend fun runCubeExample(window: Window) {
    val vertices = floatArrayOf(
        -1f, -1f, 1f, 1f, 0f, 0f,
        1f, -1f, 1f, 1f, 0f, 0f,
        1f, 1f, 1f, 1f, 0f, 0f,
        -1f, 1f, 1f, 1f, 0f, 0f,

        -1f, 1f, -1f, 0f, 0f, 1f,
        1f, 1f, -1f, 0f, 0f, 1f,
        1f, -1f, -1f, 0f, 0f, 1f,
        -1f, -1f, -1f, 0f, 0f, 1f,

        1f, -1f, -1f, .25f, .4f, .5f,
        1f, 1f, -1f, .25f, .4f, .5f,
        1f, 1f, 1f, .25f, .4f, .5f,
        1f, -1f, 1f, .25f, .4f, .5f,

        -1f, -1f, 1f, .7f, .7f, .2f,
        -1f, 1f, 1f, .7f, .7f, .2f,
        -1f, 1f, -1f, .7f, .7f, .2f,
        -1f, -1f, -1f, .7f, .7f, .2f,

        1f, 1f, -1f, .1f, .4f, .1f,
        -1f, 1f, -1f, .1f, .4f, .1f,
        -1f, 1f, 1f, .1f, .4f, .1f,
        1f, 1f, 1f, .1f, .4f, .1f,

        1f, -1f, 1f, .9f, .5f, .5f,
        -1f, -1f, 1f, .9f, .5f, .5f,
        -1f, -1f, -1f, .9f, .5f, .5f,
        1f, -1f, -1f, .9f, .5f, .5f
    )

    val indices = shortArrayOf(
        0, 1, 2, 2, 3, 0,
        4, 5, 6, 6, 7, 4,
        8, 9, 10, 10, 11, 8,
        12, 13, 14, 14, 15, 12,
        16, 17, 18, 18, 19, 16,
        20, 21, 22, 22, 23, 20
    )

    fun getProjectionMatrix(): Matrix4f {
        val windowSize = window.windowSize
        val aspectRatio = windowSize.width.toFloat() / windowSize.height

        return Matrix4f().perspective(MathUtils.toRadians(45f), aspectRatio, 1f, 10f)
    }

    val projMatrix = getProjectionMatrix()
    val viewMatrix = Matrix4f().lookAt(
        Vec3f(3.5f, 5f, 3f),
        Vec3f(0f, 0f, 0f),
        MathUtils.UNIT_Z
    )
    val transMatrix = projMatrix.mul(viewMatrix)

    val adapter = Kgpu.requestAdapterAsync(window)
    val device = adapter.requestDeviceAsync();

    val vertexShader = ShaderUtils.fromInternalTextFile(device, "cube.vert", ShaderType.VERTEX)
    val fragShader = ShaderUtils.fromInternalTextFile(device, "shared.frag", ShaderType.FRAGMENT)

    val vertexBuffer = BufferUtils.createFloatBuffer(device, "vertices", vertices, BufferUsage.VERTEX)
    val indexBuffer = BufferUtils.createShortBuffer(device, "indices", indices, BufferUsage.INDEX)
    val matrixBuffer = BufferUtils.createFloatBuffer(
        device,
        "transformation matrix",
        transMatrix.toFloats(),
        BufferUsage.UNIFORM or BufferUsage.COPY_DST
    )

    val descriptor = BindGroupLayoutDescriptor(
        BindGroupLayoutEntry(0, ShaderVisibility.VERTEX, BindingType.UNIFORM_BUFFER)
    )
    val bindGroupLayout = device.createBindGroupLayout(descriptor);
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val bindGroup = device.createBindGroup(BindGroupDescriptor(bindGroupLayout, BindGroupEntry(0, matrixBuffer)))

    val pipelineDesc = createRenderPipeline(pipelineLayout, vertexShader, fragShader, CullMode.BACK)
    val pipeline = device.createRenderPipeline(pipelineDesc)
    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM);

    var swapChain = window.configureSwapChain(swapChainDescriptor)
    window.onResize = { size : WindowSize -> 
        swapChain = window.configureSwapChain(swapChainDescriptor)
    }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView();
        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.WHITE)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setBindGroup(0, bindGroup)
        renderPassEncoder.setVertexBuffer(0, vertexBuffer)
        renderPassEncoder.setIndexBuffer(indexBuffer)
        renderPassEncoder.drawIndexed(indices.size, 1)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()

        viewMatrix.rotateZ(.01f)
        queue.writeBuffer(matrixBuffer, getProjectionMatrix().mul(viewMatrix).toBytes())
        queue.submit(cmdBuffer)
        swapChain.present();
    }
}

suspend fun runTriangleExample(window: Window) {
    mathTest()

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
    window.onResize = { size : WindowSize -> 
        swapChain = window.configureSwapChain(swapChainDescriptor)
    }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView();
        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.WHITE)
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
        1,
        0xFFFFFFFF,
        false
    )
}
