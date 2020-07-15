import io.github.kgpu.*

private object ShaderSource {
    val vertex = """
         #version 450

        layout(location=0) in vec3 a_position;
        layout(location=1) in vec2 a_tex_coords;

        layout(location=0) out vec2 v_tex_coords;
        
        layout(set = 0, binding = 2) uniform Locals {
            mat4 u_Transform;
        };

        void main() {
            v_tex_coords = a_tex_coords;
            gl_Position = u_Transform * vec4(a_position, 1.0);
        }       
    """.trimIndent()

    val frag = """
        #version 450

        layout(location=0) in vec2 v_tex_coords;
        layout(location=0) out vec4 f_color;

        layout(set = 0, binding = 0) uniform texture2D t_diffuse;
        layout(set = 0, binding = 1) uniform sampler s_diffuse;

        void main() {
            f_color = texture(sampler2D(t_diffuse, s_diffuse), v_tex_coords);
        }
    """.trimIndent()
}

suspend fun runTextureExample(window: Window) {
    fun createTransformationMatrix(): Matrix4f {
        val width = window.getWindowSize().width  / 2f
        val height = window.getWindowSize().height / 2f

        return Matrix4f().ortho(-width, width, -height, height, 10f, -10f)
    }

    val vertices = floatArrayOf(
        -128f, -128f, 1f, 0f, 0f,
        -128f, 128f, 1f, 0f, 1f,
        128f, 128f, 1f, 1f, 1f,
        128f, -128f, 1f, 1f, 0f
    )
    val indices = shortArrayOf(
        0, 1, 2,
        0, 2, 3
    )

    val image = ImageData.load("earth2D.png")

    val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
    val device = adapter.requestDeviceAsync()

    val vertexBuffer = BufferUtils.createFloatBuffer(device, vertices, BufferUsage.VERTEX)
    val indexBuffer = BufferUtils.createShortBuffer(device, indices, BufferUsage.INDEX)
    val matrixBuffer = BufferUtils.createBufferFromData(
        device, createTransformationMatrix().toBytes(), BufferUsage.UNIFORM or BufferUsage.COPY_DST
    )
    val vertexShader = ShaderUtils.fromSource(device, "vertex", ShaderSource.vertex, ShaderType.VERTEX)
    val fragShader = ShaderUtils.fromSource(device, "frag", ShaderSource.frag, ShaderType.FRAGMENT)

    val textureDesc = TextureDescriptor(
        Extent3D(image.width.toLong(), image.height.toLong(), 1),
        1,
        1,
        TextureDimension.D2,
        TextureFormat.RGBA8_UNORM_SRGB,
        TextureUsage.COPY_DST or TextureUsage.SAMPLED
    )
    val texture = device.createTexture(textureDesc)
    val textureBuffer = BufferUtils.createBufferFromData(device, image.bytes, BufferUsage.COPY_SRC)

    var cmdEncoder = device.createCommandEncoder()
    cmdEncoder.copyBufferToTexture(
        BufferCopyView(textureBuffer, image.width * 4, image.height),
        TextureCopyView(texture),
        Extent3D(image.width.toLong(), image.height.toLong(), 1)
    )
    device.getDefaultQueue().submit(cmdEncoder.finish())

    val sampler = device.createSampler(SamplerDescriptor())
    val textureView = texture.createView()

    val bindGroupLayout = device.createBindGroupLayout(
        BindGroupLayoutDescriptor(
            BindGroupLayoutEntry(
                0,
                ShaderVisibility.FRAGMENT,
                BindingType.SAMPLED_TEXTURE,
                false,
                TextureViewDimension.D2,
                TextureComponentType.FLOAT
            ),
            BindGroupLayoutEntry(
                1,
                ShaderVisibility.FRAGMENT,
                BindingType.SAMPLER,
                false
            ),
            BindGroupLayoutEntry(
                2,
                ShaderVisibility.VERTEX,
                BindingType.UNIFORM_BUFFER
            )
        )
    )
    val bindGroup = device.createBindGroup(
        BindGroupDescriptor(
            bindGroupLayout,
            BindGroupEntry(0, textureView),
            BindGroupEntry(1, sampler),
            BindGroupEntry(2, matrixBuffer)
        )
    )

    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val pipelineDesc = createRenderPipeline(pipelineLayout, vertexShader, fragShader)
    val pipeline = device.createRenderPipeline(pipelineDesc)
    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM);

    var swapChain = window.configureSwapChain(swapChainDescriptor)

    Kgpu.runLoop(window) {
        if (swapChain.isOutOfDate()) {
            swapChain = window.configureSwapChain(swapChainDescriptor)
        }

        val swapChainTexture = swapChain.getCurrentTextureView();
        cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.BLACK)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setBindGroup(0, bindGroup)
        renderPassEncoder.setVertexBuffer(0, vertexBuffer)
        renderPassEncoder.setIndexBuffer(indexBuffer)
        renderPassEncoder.drawIndexed(indices.size, 1)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.writeBuffer(matrixBuffer, createTransformationMatrix().toBytes())
        queue.submit(cmdBuffer)
        swapChain.present();
    }
}

private fun createRenderPipeline(
    pipelineLayout: PipelineLayout,
    vertexModule: ShaderModule,
    fragModule: ShaderModule
): RenderPipelineDescriptor {
    return RenderPipelineDescriptor(
        pipelineLayout,
        ProgrammableStageDescriptor(vertexModule, "main"),
        ProgrammableStageDescriptor(fragModule, "main"),
        PrimitiveTopology.TRIANGLE_LIST,
        RasterizationStateDescriptor(
            FrontFace.CCW,
            CullMode.NONE
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
                5 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttributeDescriptor(VertexFormat.FLOAT3, 0, 0),
                VertexAttributeDescriptor(VertexFormat.FLOAT2, 3 * Primitives.FLOAT_BYTES, 1)
            )
        ),
        1,
        0xFFFFFFFF,
        false
    )
}