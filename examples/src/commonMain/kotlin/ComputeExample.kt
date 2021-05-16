import io.github.kgpu.*

const val COLLATZ_SHADER =
    """
[[block]]
struct PrimeIndices {
    data: [[stride(4)]] array<u32>;
}; 

[[group(0), binding(0)]]
var<storage> v_indices: [[access(read_write)]] PrimeIndices;

fn collatz_iterations(n_base: u32) -> u32{
    var n: u32 = n_base;
    var i: u32 = 0u;
    loop {
        if (n <= 1u) {
            break;
        }
        if (n % 2u == 0u) {
            n = n / 2u;
        }
        else {
            // Overflow? (i.e. 3*n + 1 > 0xffffffffu?)
            if (n >= 1431655765u) {   // 0x55555555u
                return 4294967295u;   // 0xffffffffu
            }

            n = 3u * n + 1u;
        }
        i = i + 1u;
    }
    return i;
}

[[stage(compute), workgroup_size(1)]]
fn main([[builtin(global_invocation_id)]] global_id: vec3<u32>) {
    v_indices.data[global_id.x] = collatz_iterations(v_indices.data[global_id.x]);
}
"""

suspend fun runComputeExample() {
    val adapter = Kgpu.requestAdapterAsync()
    val device = adapter.requestDeviceAsync()
    val input = intArrayOf(2, 7, 19, 20)

    val stagingBuffer =
        device.createBuffer(
            BufferDescriptor(
                "Staging buffer",
                Primitives.INT_BYTES * input.size,
                BufferUsage.MAP_READ or BufferUsage.COPY_DST,
                false
            )
        )
    val storageBuffer =
        BufferUtils.createIntBuffer(
            device,
            "storage buffer",
            input,
            BufferUsage.STORAGE or BufferUsage.COPY_DST or BufferUsage.COPY_SRC
        )

    val bindGroupLayout =
        device.createBindGroupLayout(
            BindGroupLayoutDescriptor(
                BindGroupLayoutEntry(0, ShaderVisibility.COMPUTE, BufferBindingLayout(BufferBindingType.STORAGE))
            )
        )
    val bindGroup =
        device.createBindGroup(
            BindGroupDescriptor(bindGroupLayout, BindGroupEntry(0, BufferBinding(storageBuffer)))
        )

    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val shader = device.createShaderModule(COLLATZ_SHADER)
    val computePipeline =
        device.createComputePipeline(
            ComputePipelineDescriptor(pipelineLayout, ProgrammableStageDescriptor(shader, "main"))
        )
    val cmdEncoder = device.createCommandEncoder()
    val computePass = cmdEncoder.beginComputePass()

    computePass.setPipeline(computePipeline)
    computePass.setBindGroup(0, bindGroup)
    computePass.dispatch(input.size)
    computePass.endPass()

    cmdEncoder.copyBufferToBuffer(storageBuffer, stagingBuffer)
    device.getDefaultQueue().submit(cmdEncoder.finish())

    val times = ByteUtils.toIntArray(stagingBuffer.mapReadAsync(device).getBytes())

    setExampleStatus("Expected", "1, 16, 20, 7")
    setExampleStatus("Actual", times.joinToString())
}
