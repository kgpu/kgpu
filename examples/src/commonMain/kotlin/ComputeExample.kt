import io.github.kgpu.*

const val COLLATZ_SHADER =
    """
#version 450
layout(local_size_x = 1) in;

layout(set = 0, binding = 0) buffer PrimeIndices {
    uint[] indices;
}; // this is used as both input and output for convenience

// The Collatz Conjecture states that for any integer n:
// If n is even, n = n/2
// If n is odd, n = 3n+1
// And repeat this process for each new n, you will always eventually reach 1.
// Though the conjecture has not been proven, no counterexample has ever been found.
// This function returns how many times this recurrence needs to be applied to reach 1.
uint collatz_iterations(uint n) {
    uint i = 0;
    while(n != 1) {
        if (mod(n, 2) == 0) {
            n = n / 2;
        }
        else {
            n = (3 * n) + 1;
        }
        i++;
    }
    return i;
}

void main() {
    uint index = gl_GlobalInvocationID.x;
    indices[index] = collatz_iterations(indices[index]);
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
                false))
    val storageBuffer =
        BufferUtils.createIntBuffer(
            device,
            "storage buffer",
            input,
            BufferUsage.STORAGE or BufferUsage.COPY_DST or BufferUsage.COPY_SRC)

    val bindGroupLayout =
        device.createBindGroupLayout(
            BindGroupLayoutDescriptor(
                BindGroupLayoutEntry(0, ShaderVisibility.COMPUTE, BindingType.STORAGE_BUFFER)))
    val bindGroup =
        device.createBindGroup(
            BindGroupDescriptor(bindGroupLayout, BindGroupEntry(0, storageBuffer)))

    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val shader = TODO()
    val computePipeline =
        device.createComputePipeline(
            ComputePipelineDescriptor(pipelineLayout, ProgrammableStageDescriptor(shader, "main")))
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
