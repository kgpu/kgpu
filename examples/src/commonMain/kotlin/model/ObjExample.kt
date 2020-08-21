package model

import io.github.kgpu.*
import io.github.kgpu.kshader.*
import io.github.kgpu.kcgmath.*;
import io.github.kgpu.kcgmath.MathUtils;

val VERTEX_SHADER = """
#version 450

out gl_PerVertex {
    vec4 gl_Position;
};

layout(location=0) in vec3 positions;

layout(set = 0, binding = 0) uniform Locals {
    mat4 u_Transform;
};

void main() {
    gl_Position = u_Transform * vec4(positions, 1.0);
}
""".trimIndent()

val FRAG_SHADER = """
#version 450

layout(location = 0) out vec4 outColor;

void main() {
    outColor = vec4(1.0, 0.0, 0.0, 1.0);
}
""".trimIndent()

suspend fun runObjModelExample(window: Window) {
    fun getProjectionMatrix(): Matrix4 {
        val windowSize = window.windowSize
        val aspectRatio = windowSize.width.toFloat() / windowSize.height

        return Matrix4().perspective(45f / 180f * MathUtils.PIf, aspectRatio, 1f, 10f)
    }

    val text = KgpuFiles.loadInternalUtf8("models/model.obj")
    val model = Model(text)
    val projMatrix = getProjectionMatrix()
    val viewMatrix = Matrix4().lookAt(
        Vec3(3.5f, 5f, 3f),
        Vec3(0f, 0f, 0f),
        Vec3(0f, 1f, 0f)
    )
    val transMatrix = projMatrix.mul(viewMatrix)
    
    val adapter = Kgpu.requestAdapterAsync(window)
    val device = adapter.requestDeviceAsync()
    val indices = BufferUtils.createIntBuffer(device, "Model Indices", model.indices.toIntArray(), BufferUsage.INDEX)
    val vertices = BufferUtils.createFloatBuffer(device, "Model Indices", model.getVertexArray(), BufferUsage.VERTEX)
    val matrixBuffer = BufferUtils.createFloatBuffer(
        device,
        "transformation matrix",
        transMatrix.toFloats(),
        BufferUsage.UNIFORM or BufferUsage.COPY_DST
    )

    val vertexShader = device.createShaderModule(KShader.compile("vertex", VERTEX_SHADER, KShaderType.VERTEX))
    val fragShader = device.createShaderModule(KShader.compile("frag", FRAG_SHADER, KShaderType.FRAGMENT))

    val descriptor = BindGroupLayoutDescriptor(
        BindGroupLayoutEntry(0, ShaderVisibility.VERTEX, BindingType.UNIFORM_BUFFER)
    )
    val bindGroupLayout = device.createBindGroupLayout(descriptor);
    val bindGroup = device.createBindGroup(BindGroupDescriptor(bindGroupLayout, BindGroupEntry(0, matrixBuffer)))
    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val pipeline = device.createRenderPipeline(createRenderPipeline(pipelineLayout, vertexShader, fragShader))
    val swapChainDesc = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM)
    var swapChain = window.configureSwapChain(swapChainDesc)

    window.onResize = {
        swapChain = window.configureSwapChain(swapChainDesc)
    }

    Kgpu.runLoop(window) {
        val swapChainTexture = swapChain.getCurrentTextureView();
        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.WHITE)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.setBindGroup(0, bindGroup)
        renderPassEncoder.setVertexBuffer(0, vertices)
        renderPassEncoder.setIndexBuffer(indices)
        renderPassEncoder.drawIndexed(model.indices.size, 1)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()

        viewMatrix.rotate(.01f, 0f, .01f)
        queue.writeBuffer(matrixBuffer, getProjectionMatrix().mul(viewMatrix).toBytes())
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
            IndexFormat.UINT32, VertexBufferLayoutDescriptor(
                3 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttributeDescriptor(VertexFormat.FLOAT3, 0, 0)
            )
        ),
        1,
        0xFFFFFFFF,
        false
    )
}