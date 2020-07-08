import io.github.kgpu.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    Kgpu.init();

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    GlobalScope.launch {
        suspend {
            val adapter = window.requestAdapterAsync(PowerPreference.DEFAULT)
            println("Adapter: $adapter")
            val device = adapter.requestDeviceAsync();
            println("Device: $device")

            val vertexShader = KgpuFiles.loadInternal("/triangle.vert.spv")
            val vertexModule = device.createShaderModule(vertexShader)
            val fragShader = KgpuFiles.loadInternal("/triangle.frag.spv")
            val fragModule = device.createShaderModule(fragShader)

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
                    null,
                    VertexStateDescriptor(IndexFormat.UINT16, emptyArray()),
                    1,
                    0,
                    false
            )

            var pipeline = device.createRenderPipeline(pipelineDesc)
            println("Pipeline: $pipeline")
        }.invoke()

        Kgpu.runLoop(window) {
        }
    }

    Thread.sleep(5000)
}
