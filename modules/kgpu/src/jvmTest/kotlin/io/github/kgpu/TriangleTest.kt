package io.github.kgpu

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.kgpu.*
import io.github.kgpu.kshader.*

/**
 * Renders a triangle to a texture and then checks the texture
 */
class TriangleTest {

    companion object {
        val VERTEX_SHADER = """
            #version 450

            out gl_PerVertex {
                vec4 gl_Position;
            };

            void main() {
                vec2 pos = vec2(gl_VertexIndex == 2 ? 3.0 : -1.0, gl_VertexIndex == 1 ? 3.0 : -1.0);
                gl_Position = vec4(pos, 0.0, 1.0);
            } 
        """.trimIndent()

        val FRAG_SHADER = """
            #version 450

            layout(location = 0) out vec4 outColor;

            void main() {
                outColor = vec4(1.0, 1.0, 1.0, 1.0);
            } 
        """.trimIndent()
    }

    suspend fun triangleTestAsync(){
        Kgpu.init(false)
        val size = 64L;

        val adapter = Kgpu.requestAdapterAsync()
        val device = adapter.requestDeviceAsync();
        val vertexShader = device.createShaderModule(KShader.compile("vertex", TriangleTest.VERTEX_SHADER, KShaderType.VERTEX))
        val fragShader = device.createShaderModule(KShader.compile("frag", TriangleTest.FRAG_SHADER, KShaderType.FRAGMENT))

        val texture = device.createTexture(TextureDescriptor(
            Extent3D(size, size, 1),
            1, 
            1,
            TextureDimension.D2,
            TextureFormat.RGBA8_UNORM,
            27
        ))

        val textureView = texture.createView()
        val buffer = device.createBuffer(BufferDescriptor(
            "Output Buffer",
            16384,
            9,
            false
        ))
        val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor())
        val pipelineDesc = createRenderPipeline(pipelineLayout, vertexShader, fragShader)
        val pipeline = device.createRenderPipeline(pipelineDesc)

        val cmdEncoder = device.createCommandEncoder();

        val colorAttachment = RenderPassColorAttachmentDescriptor(textureView, Color.BLACK)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(pipeline)
        renderPassEncoder.draw(3, 1)
        renderPassEncoder.endPass()

        cmdEncoder.copyTextureToBuffer(
            TextureCopyView(texture),
            BufferCopyView(buffer, size.toInt() * 4, size.toInt()),
            Extent3D(size, size, 1)
        )

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(cmdBuffer)

        val output = buffer.mapReadAsync(device).getBytes()

        for(byte in output){
            if(byte != 0xFF.toByte()){
                throw RuntimeException("Expected 255 found: " + byte.toUByte());
            }
        }
    }

    @Test
    fun triangleTest(){
        runBlocking {
            triangleTestAsync();
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
                    TextureFormat.RGBA8_UNORM,
                    BlendDescriptor(),
                    BlendDescriptor(),
                    0xF
                )
            ),
            Kgpu.undefined,
            VertexStateDescriptor(IndexFormat.UINT16),
            1,
            0xFFFFFFFF,
            false
        )
    }
}