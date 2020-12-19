package boid

import io.github.kgpu.*
import io.github.kgpu.kshader.KShader
import io.github.kgpu.kshader.KShaderType
import kotlin.random.Random

const val BOIDS_PER_GROUP = 64

const val BOID_COUNT = 1024

const val MASS_RULE_DISTANCE = .1

const val MASS_RULE_SCALE = 0.02

const val SEPARATION_RULE_DISTANCE = .025

const val SEPARATION_RULE_SCALE = .05

const val HEADING_RULE_DISTANCE = .025

const val HEADING_RULE_SCALE = .005

object Shaders {
    val VERTEX =
        """
        #version 450

        out gl_PerVertex {
            vec4 gl_Position;
        };

        layout(location=0) in vec2 vertex;
        layout(location=1) in vec2 position;
        layout(location=2) in vec2 velocity;

        void main() {
            float angle = atan(velocity.y, velocity.x);
            vec2 rot_vertex = vec2(vertex.x * cos(angle) - vertex.y * sin(angle),
                    vertex.x * sin(angle) + vertex.y * cos(angle));
            gl_Position = vec4(position + rot_vertex, 0.0, 1.0);
        }
    """.trimIndent()

    val FRAG =
        """
        #version 450

        layout(location = 0) out vec4 outColor;

        void main() {
            outColor = vec4(1.0, 1.0, 1.0, 1.0);
        }

    """.trimIndent()

    val COMPUTE =
        """
        #version 450

        #define BOID_COUNT $BOID_COUNT
        #define BOIDS_PER_GROUP $BOIDS_PER_GROUP
        
        layout(local_size_x = BOIDS_PER_GROUP) in;

        struct Boid {
            vec2 pos;
            vec2 vel;
        };
        layout(std140, set = 0, binding = 0) buffer SrcBoids {
            Boid boids[BOID_COUNT];
        } srcBoids;
        layout(std140, set = 0, binding = 1) buffer DstBoids {
            Boid boids[BOID_COUNT];
        } dstBoids;

        void main() {
            uint index = gl_GlobalInvocationID.x;
            
            if(index > BOID_COUNT) {
                return;
            }
            
            vec2 srcPos = srcBoids.boids[index].pos;
            vec2 srcVel = srcBoids.boids[index].vel;
            vec2 pos;
            vec2 vel;
            vec2 cMass = vec2(0, 0);
            vec2 cVel = vec2(0, 0);
            vec2 colVel = vec2(0, 0);
            int cMassCount = 0;
            int cVelCount = 0;
            
            for(int i = 0; i < BOID_COUNT; i++) {
                if(i == index) {
                    continue;
                }
                
                pos = srcBoids.boids[i].pos;
                vel = srcBoids.boids[i].vel;
                
                if (distance(pos, srcPos) < $MASS_RULE_DISTANCE) {
                    cMass += pos;
                    cMassCount++;
                }
                if (distance(pos, srcPos) < $SEPARATION_RULE_DISTANCE) {
                    colVel -= (pos - srcPos);
                }
                if (distance(pos, srcPos) < $HEADING_RULE_DISTANCE) {
                    cVel += vel;
                    cVelCount++;
                }
            }
            
            if (cMassCount > 0){
                cMass = cMass / cMassCount - srcPos;
            }
            if (cVelCount > 0){
                cVel = cVel / cVelCount;
            }
            
            srcVel += cMass * $MASS_RULE_SCALE + colVel * $SEPARATION_RULE_SCALE + cVel * $HEADING_RULE_SCALE;
            srcVel = normalize(srcVel) * clamp(length(srcVel), 0.0, 0.1);
            srcPos += srcVel / 50; 
            
            if(srcPos.x < -1.0) {
                srcPos.x = 1;
            }
            if(srcPos.x > 1.0) {
                srcPos.x = -1;
            }
            if(srcPos.y < -1.0) {
                srcPos.y = 1;
            }
            if(srcPos.y > 1.0) {
                srcPos.y = -1;
            }

            dstBoids.boids[index].pos = srcPos;
            dstBoids.boids[index].vel = srcVel;
        }
    """.trimIndent()
}

suspend fun runBoidExample(window: Window) {
    val adapter = Kgpu.requestAdapterAsync(window)
    val device = adapter.requestDeviceAsync()
    val vertexShader =
        device.createShaderModule(KShader.compile("vertex", Shaders.VERTEX, KShaderType.VERTEX))
    val fragShader =
        device.createShaderModule(KShader.compile("frag", Shaders.FRAG, KShaderType.FRAGMENT))
    val computeShader =
        device.createShaderModule(KShader.compile("compute", Shaders.COMPUTE, KShaderType.COMPUTE))

    val vertices =
        floatArrayOf(
            -0.015f,
            -0.0075f,
            -0.015f,
            .0075f,
            0.015f,
            0.00f,
        )
    val vertexBuffer =
        BufferUtils.createFloatBuffer(device, "vertices", vertices, BufferUsage.VERTEX)
    val boidData = FloatArray(BOID_COUNT * 4)
    for (index in 0 until BOID_COUNT) {
        boidData[index * 4] = Random.Default.nextFloat() * 2f - 1f
        boidData[index * 4 + 1] = Random.Default.nextFloat() * 2f - 1f
        boidData[index * 4 + 2] = (Random.Default.nextFloat() - .5f) / 10f
        boidData[index * 4 + 3] = (Random.Default.nextFloat() - .5f) / 10f
    }

    val boidBuffers = ArrayList<Buffer>(2)
    val boidBindGroups = ArrayList<BindGroup>(2)
    val bindGroupLayout =
        device.createBindGroupLayout(
            BindGroupLayoutDescriptor(
                BindGroupLayoutEntry(
                    0, ShaderVisibility.COMPUTE, BindingType.STORAGE_BUFFER, false),
                BindGroupLayoutEntry(
                    1, ShaderVisibility.COMPUTE, BindingType.STORAGE_BUFFER, false),
            ))

    for (i in 0..1) {
        boidBuffers.add(
            BufferUtils.createFloatBuffer(
                device,
                "Boid Buffer $i",
                boidData,
                BufferUsage.VERTEX or BufferUsage.STORAGE or BufferUsage.COPY_DST))
    }

    for (i in 0..1) {
        boidBindGroups.add(
            device.createBindGroup(
                BindGroupDescriptor(
                    bindGroupLayout,
                    BindGroupEntry(0, boidBuffers[i]),
                    BindGroupEntry(1, boidBuffers[(i + 1) % 2]))))
    }

    val renderPipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor())
    val computePipelineLayout =
        device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))

    val computePipelineDesc =
        ComputePipelineDescriptor(
            computePipelineLayout, ProgrammableStageDescriptor(computeShader, "main"))
    val computePipeline = device.createComputePipeline(computePipelineDesc)
    val renderPipeline =
        device.createRenderPipeline(
            createRenderPipeline(renderPipelineLayout, vertexShader, fragShader))
    val swapChainDescriptor = SwapChainDescriptor(device, TextureFormat.BGRA8_UNORM)

    var swapChain = window.configureSwapChain(swapChainDescriptor)
    window.onResize =
        { size: WindowSize ->
            swapChain = window.configureSwapChain(swapChainDescriptor)
        }

    var frameCount = 0
    Kgpu.runLoop(window) {
        val cmdEncoder = device.createCommandEncoder()
        val computePass = cmdEncoder.beginComputePass()

        computePass.setPipeline(computePipeline)
        computePass.setBindGroup(0, boidBindGroups[frameCount % 2])
        computePass.dispatch(BOID_COUNT / BOIDS_PER_GROUP, 1)
        computePass.endPass()

        val swapChainTexture = swapChain.getCurrentTextureView()
        val colorAttachment = RenderPassColorAttachmentDescriptor(swapChainTexture, Color.BLACK)
        val renderPassEncoder = cmdEncoder.beginRenderPass(RenderPassDescriptor(colorAttachment))
        renderPassEncoder.setPipeline(renderPipeline)
        renderPassEncoder.setVertexBuffer(0, vertexBuffer)
        renderPassEncoder.setVertexBuffer(1, boidBuffers[++frameCount % 2])
        renderPassEncoder.draw(3, BOID_COUNT)
        renderPassEncoder.endPass()

        val cmdBuffer = cmdEncoder.finish()
        val queue = device.getDefaultQueue()
        queue.submit(cmdBuffer)
        swapChain.present()
    }
}

private fun createRenderPipeline(
    pipelineLayout: PipelineLayout,
    vertexModule: ShaderModule,
    fragModule: ShaderModule,
): RenderPipelineDescriptor {

    return RenderPipelineDescriptor(
        pipelineLayout,
        ProgrammableStageDescriptor(vertexModule, "main"),
        ProgrammableStageDescriptor(fragModule, "main"),
        PrimitiveTopology.TRIANGLE_LIST,
        RasterizationStateDescriptor(FrontFace.CCW, CullMode.NONE),
        arrayOf(
            ColorStateDescriptor(
                TextureFormat.BGRA8_UNORM, BlendDescriptor(), BlendDescriptor(), 0xF)),
        Kgpu.undefined,
        VertexStateDescriptor(
            null,
            VertexBufferLayoutDescriptor(
                2 * Primitives.FLOAT_BYTES,
                InputStepMode.VERTEX,
                VertexAttributeDescriptor(VertexFormat.FLOAT2, 0, 0),
            ),
            VertexBufferLayoutDescriptor(
                4 * Primitives.FLOAT_BYTES,
                InputStepMode.INSTANCE,
                VertexAttributeDescriptor(VertexFormat.FLOAT2, 0, 1),
                VertexAttributeDescriptor(VertexFormat.FLOAT2, 8, 2),
            ),
        ),
        1,
        0xFFFFFFFF,
        false)
}
