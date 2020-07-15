import io.github.kgpu.*
import kotlin.math.cos
import kotlin.math.sin


private object EarthShaderSource {

    val vertex = """
        #version 450

        out gl_PerVertex {
            vec4 gl_Position;
        };
        layout(location=0) in vec3 v_position;
        layout(location=1) in vec2 v_tex_coords;
        layout(location=2) in vec3 v_normal;
        layout(location=0) out vec2 f_tex_coords;
        layout(location=1) out vec3 f_normal;
        layout(location=2) out vec3 f_position;

        layout(set = 0, binding = 2) uniform TransformationMatrix {
            mat4 u_Transform;
        };

        layout(set = 0, binding = 3) uniform NormalMatrix {
                    mat4 u_NormalMatrix;
        };

        layout(set = 0, binding = 4) uniform ModelMatrix {
            mat4 u_Model;
        };

        void main() {
            f_tex_coords = v_tex_coords;
            f_normal = vec3(vec4(v_normal, 1.0) * u_NormalMatrix);
            f_position = vec3(vec4(v_position, 1.0) * u_Model);
            gl_Position = u_Transform * vec4(f_position, 1.0);
        }
    """.trimIndent()

    val frag = """
        #version 450

        layout(location=0) in vec2 tex_coords;
        layout(location=1) in vec3 normal;
        layout(location=2) in vec3 position;
        layout(location=0) out vec4 f_color;

        layout(set = 0, binding = 0) uniform texture2D u_texture;
        layout(set = 0, binding = 1) uniform sampler u_sampler;

        void main() {
            vec3 light_src = vec3(-3, -3, 2);
            vec4 ambient = vec4(1.0, 1.0, 1.0, 1.0);
            vec4 light_color = vec4(1.0, 1.0, 1.0, 1.0);
            vec3 light = normalize(light_src - position);
            vec4 directional = max(dot(normal, light), 0.0) * light_color;

            f_color = (ambient + directional) * texture(sampler2D(u_texture, u_sampler), tex_coords);
        }
    """.trimIndent()
}

suspend fun runEarthExample(window: Window) {
    fun createTransformationMatrix(viewMatrix: Matrix4f): Matrix4f {
        val windowSize = window.getWindowSize()
        val aspectRatio = windowSize.width.toFloat() / windowSize.height

        return Matrix4f().perspective(MathUtils.toRadians(45f), aspectRatio, 1f, 10f).mul(viewMatrix)
    }

    fun createNormalMatrix(modelMatrix: Matrix4f, viewMatrix: Matrix4f): Matrix4f {
        return Matrix4f(modelMatrix).invert().transpose().mul(viewMatrix)
    }


    fun createMatrixBuffer(device: Device, matrix: Matrix4f): Buffer {
        return BufferUtils.createBufferFromData(
            device,
            createTransformationMatrix(matrix).toBytes(),
            BufferUsage.UNIFORM or BufferUsage.COPY_DST
        )
    }

    val earth = Sphere(40, 40)
    val indices = earth.generateIndices()
    val vertices = earth.generateVertices()
    val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
    val device = adapter.requestDeviceAsync()
    val image = ImageData.load("earth3D.png")
    val modelMatrix = Matrix4f().rotateY(.1f)
    val viewMatrix = Matrix4f().lookAt(
        Vec3f(-5f, -5f, 3.5f),
        Vec3f(),
        MathUtils.UNIT_Z
    )

    val vertexShader = ShaderUtils.fromSource(device, "vertex", EarthShaderSource.vertex, ShaderType.VERTEX)
    val fragShader = ShaderUtils.fromSource(device, "frag", EarthShaderSource.frag, ShaderType.FRAGMENT)
    val vertexBuffer = BufferUtils.createFloatBuffer(device, vertices, BufferUsage.VERTEX)
    val indexBuffer = BufferUtils.createShortBuffer(device, indices, BufferUsage.INDEX)
    val modelMatrixBuffer = createMatrixBuffer(device, modelMatrix)
    val normalMatrixBuffer = createMatrixBuffer(device, createNormalMatrix(modelMatrix, viewMatrix))
    val transformationMatrixBuffer = createMatrixBuffer(device, createTransformationMatrix(viewMatrix))

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
            BindGroupLayoutEntry(1, ShaderVisibility.FRAGMENT, BindingType.SAMPLER, false),
            BindGroupLayoutEntry(2, ShaderVisibility.VERTEX, BindingType.UNIFORM_BUFFER),
            BindGroupLayoutEntry(3, ShaderVisibility.VERTEX, BindingType.UNIFORM_BUFFER),
            BindGroupLayoutEntry(4, ShaderVisibility.VERTEX, BindingType.UNIFORM_BUFFER)
        )
    )
    val bindGroup = device.createBindGroup(
        BindGroupDescriptor(
            bindGroupLayout,
            BindGroupEntry(0, textureView),
            BindGroupEntry(1, sampler),
            BindGroupEntry(2, transformationMatrixBuffer),
            BindGroupEntry(3, normalMatrixBuffer),
            BindGroupEntry(4, modelMatrixBuffer)
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
        modelMatrix.rotateZ(.01f)
        queue.writeBuffer(modelMatrixBuffer, modelMatrix.toBytes())
        queue.writeBuffer(transformationMatrixBuffer, createTransformationMatrix(viewMatrix).toBytes())
        queue.writeBuffer(normalMatrixBuffer, createNormalMatrix(modelMatrix, viewMatrix).toBytes())
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
            CullMode.BACK
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
                8 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttributeDescriptor(VertexFormat.FLOAT3, 0, 0),
                VertexAttributeDescriptor(VertexFormat.FLOAT2, 3 * Primitives.FLOAT_BYTES, 1),
                VertexAttributeDescriptor(VertexFormat.FLOAT3, 5 * Primitives.FLOAT_BYTES, 2)
            )
        ),
        1,
        0xFFFFFFFF,
        false
    )
}

private class Sphere(private val chunks: Int, private val slices: Int, val radius: Float = 2f) {
    companion object {
        const val FLOATS_PER_VERTEX = 8
    }

    init {
        if ((chunks + 1) * (slices + 1) > Short.MAX_VALUE) {
            throw UnsupportedOperationException("Too many vertices!")
        }
    }

    fun generateVertices(): FloatArray {
        val vertices = FloatArray((chunks + 1) * (slices + 1) * FLOATS_PER_VERTEX)
        var angleXY: Float
        var angleZ: Float
        for (slice in 0..slices) {
            angleZ = MathUtils.PIf / slices * slice - MathUtils.PIf / 2f

            //last vertex overlaps the first one, but with the other end of the texture
            for (chunk in 0..chunks) {
                val index: Int = (slice * (chunks + 1) + chunk) * FLOATS_PER_VERTEX
                angleXY = MathUtils.PIf * 2f / chunks * chunk
                val pos: Vec3f = Vec3f(
                    cos(angleZ) * cos(angleXY),
                    cos(angleZ) * sin(angleXY),
                    sin(angleZ)
                ).mul(radius)
                vertices[index] = pos.x
                vertices[index + 1] = pos.y
                vertices[index + 2] = pos.z
                vertices[index + 3] = chunk.toFloat() / chunks
                vertices[index + 4] = slice.toFloat() / slices
                pos.normalize()
                vertices[index + 5] = pos.x
                vertices[index + 6] = pos.y
                vertices[index + 7] = pos.z
            }
        }
        return vertices
    }

    fun generateIndices(): ShortArray {
        val indices = ShortArray(6 * chunks * slices)
        for (slice in 0 until slices) {
            val sliceIndex = (chunks + 1) * slice
            val nextSliceIndex = (chunks + 1) * (slice + 1)
            for (chunk in 0 until chunks) {
                val index = (slice * chunks + chunk) * 6
                indices[index] = (sliceIndex + chunk).toShort()
                indices[index + 1] = (sliceIndex + chunk + 1).toShort()
                indices[index + 2] = (nextSliceIndex + chunk).toShort()
                indices[index + 3] = (nextSliceIndex + chunk).toShort()
                indices[index + 4] = (sliceIndex + chunk + 1).toShort()
                indices[index + 5] = (nextSliceIndex + chunk + 1).toShort()
            }
        }
        return indices
    }
}