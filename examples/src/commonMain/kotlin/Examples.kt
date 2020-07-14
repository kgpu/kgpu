import io.github.kgpu.*

fun toByteArray(floatArray: FloatArray): ByteArray {
    val bytes = ByteArray(floatArray.size * 4)
    floatArray.forEachIndexed { index, float ->
        run {
            val i = index * 4
            val bits = float.toRawBits()

            bytes[i + 3] = (bits shr 24).toByte()
            bytes[i + 2] = (bits shr 16).toByte()
            bytes[i + 1] = (bits shr 8).toByte()
            bytes[i + 0] = bits.toByte()
        }
    }

    return bytes
}

fun toByteArray(shortArray: ShortArray): ByteArray {
    val bytes = ByteArray(shortArray.size * 2)
    shortArray.forEachIndexed { index, value ->
        run {
            val i = index * 2

            bytes[i + 1] = (value.toInt() shr 8).toByte()
            bytes[i + 0] = value.toByte()
        }
    }

    return bytes
}

fun mathTest(){
    val matrix = Matrix4f().translate(1f, 2f, 3f)

    println("Op0 = ${matrix.toFloats().joinToString()}")
}

suspend fun runCubeExample(window: Window){
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

    val windowSize = window.getWindowSize()
    val aspectRatio = windowSize.width.toFloat() / windowSize.height
    val projMatrix = Matrix4f().perspective(MathUtils.toRadians(45f), aspectRatio, 1f, 10f)

    val viewMatrix = Matrix4f().lookAt(
        Vec3f(3.5f, 5f, 3f),
        Vec3f(0f, 0f, 0f),
        MathUtils.UNIT_Z
    )
    val transMatrix = projMatrix.mul(viewMatrix)

    val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
    val device = adapter.requestDeviceAsync();

    val vertexShaderSrc = KgpuFiles.loadInternalUtf8("cube.vert")
    val vertexShader = ShaderCompiler.compile("vertex", vertexShaderSrc, ShaderType.VERTEX)
    val vertexModule = device.createShaderModule(vertexShader)
    val fragShaderSrc = KgpuFiles.loadInternalUtf8("shared.frag")
    val fragShader = ShaderCompiler.compile("frag", fragShaderSrc, ShaderType.FRAGMENT)
    val fragModule = device.createShaderModule(fragShader)

    val vertexBufferSize = vertices.size * Primitives.FLOAT_BYTES
    val vertexBuffer = device.createBufferWithData(
        BufferDescriptor(
            vertexBufferSize,
            BufferUsage.VERTEX,
            true
        ),
        toByteArray(vertices)
    )

    val indexBufferSize = indices.size * Primitives.FLOAT_BYTES
    val indexBuffer = device.createBufferWithData(
        BufferDescriptor(
            vertexBufferSize,
            BufferUsage.INDEX,
            true
        ),
        toByteArray(indices)
    )

    val matrixBufferSize = transMatrix.toFloats().size * Primitives.FLOAT_BYTES
    val matrixBuffer = device.createBufferWithData(
        BufferDescriptor(
            matrixBufferSize,
            BufferUsage.UNIFORM,
            true
        ),
        toByteArray(transMatrix.toFloats())
    )

    val descriptor = BindGroupLayoutDescriptor(
        arrayOf(
            BindGroupLayoutEntry(0, ShaderStage.VERTEX, BindingType.UNIFORM_BUFFER)
        )
    )
    val bindGroupLayout = device.createBindGroupLayout(descriptor);
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(arrayOf(bindGroupLayout)))
    val bindGroup = device.createBindGroup(BindGroupDescriptor(
        bindGroupLayout,
        arrayOf(
            BindGroupEntry(0, matrixBuffer)
        )
    ))

    val pipelineDesc = RenderPipelineDescriptor(
        pipelineLayout,
        ProgrammableStageDescriptor(vertexModule, "main"),
        ProgrammableStageDescriptor(fragModule, "main"),
        PrimitiveTopology.TRIANGLE_LIST,
        RasterizationStateDescriptor(
            FrontFace.CCW,
            CullMode.BACK,
            false,
            0,
            0f,
            0f
        ),
        arrayOf(
            ColorStateDescriptor(
                TextureFormat.BGRA8_UNORM,
                BlendDescriptor(
                    BlendFactor.ONE,
                    BlendFactor.ZERO,
                    BlendOperation.ADD
                ),
                BlendDescriptor(
                    BlendFactor.ONE,
                    BlendFactor.ZERO,
                    BlendOperation.ADD
                ),
                0xF
            )
        ),
        Kgpu.undefined,
        VertexStateDescriptor(
            IndexFormat.UINT16, arrayOf(
                VertexBufferLayoutDescriptor(
                    6 * Primitives.FLOAT_BYTES,
                    InputStepMode.VERTEX,
                    arrayOf(
                        VertexAttributeDescriptor(
                            VertexFormat.FLOAT3,
                            0,
                            0
                        ),
                        VertexAttributeDescriptor(
                            VertexFormat.FLOAT3,
                            3 * Primitives.FLOAT_BYTES,
                            1
                        )
                    )
                )
            )
        ),
        1,
        0xFFFFFFFF,
        false
    )
    val pipeline = device.createRenderPipeline(pipelineDesc)
    println("test5")

    var swapChain = window.configureSwapChain(
        SwapChainDescriptor(
            device,
            TextureFormat.BGRA8_UNORM,
            TextureUsage.OUTPUT_ATTACHMENT
        )
    )

    Kgpu.runLoop(window) {
        if (swapChain.isOutOfDate()) {
            swapChain = window.configureSwapChain(
                SwapChainDescriptor(
                    device,
                    TextureFormat.BGRA8_UNORM,
                    TextureUsage.OUTPUT_ATTACHMENT
                )
            )
        }

        val swapChainTexture = swapChain.getCurrentTextureView();
        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(
            swapChainTexture,
            Pair(LoadOp.CLEAR, Color.WHITE),
            StoreOp.STORE
        )
        val renderPassEncoder = cmdEncoder.beginRenderPass(
            RenderPassDescriptor(
                arrayOf(colorAttachment)
            )
        )
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setBindGroup(0, bindGroup)
        renderPassEncoder.setVertexBuffer(0, vertexBuffer, 0, vertexBufferSize)
        renderPassEncoder.setIndexBuffer(indexBuffer, 0, indexBufferSize)
        renderPassEncoder.drawIndexed(indices.size, 1, 0, 0, 0)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(arrayOf(cmdBuffer))
        swapChain.present();
    }
}

suspend fun runTriangleExample(window: Window) {
    mathTest()

    val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
    val device = adapter.requestDeviceAsync();

    val vertexShaderSrc = KgpuFiles.loadInternalUtf8("triangle.vert")
    val vertexShader = ShaderCompiler.compile("vertex", vertexShaderSrc, ShaderType.VERTEX)
    val vertexModule = device.createShaderModule(vertexShader)
    val fragShaderSrc = KgpuFiles.loadInternalUtf8("shared.frag")
    val fragShader = ShaderCompiler.compile("frag", fragShaderSrc, ShaderType.FRAGMENT)
    val fragModule = device.createShaderModule(fragShader)

    val positions = floatArrayOf(
        -.5f, .5f, 0f, 1f, 0f, 0f,
        .5f, .5f, 0f, 0f, 1f, 0f,
        0f, -.5f, 0f, 0f, 0f, 1f
    )
    val bufferSize = positions.size * Primitives.FLOAT_BYTES
    val buffer = device.createBufferWithData(
        BufferDescriptor(
            bufferSize,
            BufferUsage.VERTEX,
            true
        ),
        toByteArray(positions)
    )

    val layouts = emptyArray<BindGroupLayout>()
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(layouts))

    val pipelineDesc = RenderPipelineDescriptor(
        pipelineLayout,
        ProgrammableStageDescriptor(vertexModule, "main"),
        ProgrammableStageDescriptor(fragModule, "main"),
        PrimitiveTopology.TRIANGLE_LIST,
        RasterizationStateDescriptor(
            FrontFace.CCW,
            CullMode.NONE,
            false,
            0,
            0f,
            0f
        ),
        arrayOf(
            ColorStateDescriptor(
                TextureFormat.BGRA8_UNORM,
                BlendDescriptor(
                    BlendFactor.ONE,
                    BlendFactor.ZERO,
                    BlendOperation.ADD
                ),
                BlendDescriptor(
                    BlendFactor.ONE,
                    BlendFactor.ZERO,
                    BlendOperation.ADD
                ),
                0xF
            )
        ),
        Kgpu.undefined,
        VertexStateDescriptor(
            IndexFormat.UINT16, arrayOf(
                VertexBufferLayoutDescriptor(
                    6 * Primitives.FLOAT_BYTES,
                    InputStepMode.VERTEX,
                    arrayOf(
                        VertexAttributeDescriptor(
                            VertexFormat.FLOAT3,
                            0,
                            0
                        ),
                        VertexAttributeDescriptor(
                            VertexFormat.FLOAT3,
                            3 * Primitives.FLOAT_BYTES,
                            1
                        )
                    )
                )
            )
        ),
        1,
        0xFFFFFFFF,
        false
    )
    val pipeline = device.createRenderPipeline(pipelineDesc)

    var swapChain = window.configureSwapChain(
        SwapChainDescriptor(
            device,
            TextureFormat.BGRA8_UNORM,
            TextureUsage.OUTPUT_ATTACHMENT
        )
    )

    Kgpu.runLoop(window) {
        if (swapChain.isOutOfDate()) {
            swapChain = window.configureSwapChain(
                SwapChainDescriptor(
                    device,
                    TextureFormat.BGRA8_UNORM,
                    TextureUsage.OUTPUT_ATTACHMENT
                )
            )
        }

        val swapChainTexture = swapChain.getCurrentTextureView();
        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(
            swapChainTexture,
            Pair(LoadOp.CLEAR, Color.WHITE),
            StoreOp.STORE
        )
        val renderPassEncoder = cmdEncoder.beginRenderPass(
            RenderPassDescriptor(
                arrayOf(colorAttachment)
            )
        )
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setVertexBuffer(0, buffer, 0, bufferSize)
        renderPassEncoder.draw(3, 1, 0, 0)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(arrayOf(cmdBuffer))
        swapChain.present();
    }
}