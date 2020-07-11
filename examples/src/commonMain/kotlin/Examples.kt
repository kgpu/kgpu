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

suspend fun runExample(window: Window) {
    val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
    println("Adapter: $adapter")
    val device = adapter.requestDeviceAsync();
    println("Device: $device")

    val vertexShader = KgpuFiles.loadInternal("/triangle.vert.spv")
    val vertexModule = device.createShaderModule(vertexShader)
    val fragShader = KgpuFiles.loadInternal("/triangle.frag.spv")
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
    println("Buffer: $buffer")

    println("Vertex Shader: $vertexModule")
    println("Fragment Shader: $fragModule")

    val layouts = emptyArray<BindGroupLayout>()
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(layouts))

    println("Pipeline Layout: $pipelineLayout")

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
    println("Pipeline Descriptor: $pipelineDesc")

    val pipeline = device.createRenderPipeline(pipelineDesc)
    println("Pipeline: $pipeline")

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