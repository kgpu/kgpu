package compute

import io.github.kgpu.*;
import kotlin.random.Random;
import kotlin.math.abs;


// Not on the slides. Local size in X and Y. Without this the GPU will only run
// one instance of the compute shader on a block of (for example) 32 ALUs,
// wasting 31 of them.
private const val LOCAL_SIZE = 8;
private const val MATRIX_SIZE = 512
private val SHADER_SOURCE = """
#version 450

layout(std430, set = 0, binding = 0) readonly buffer MatrixA {
    float matrixA[];
};
layout(std430, set = 0, binding = 1) readonly buffer MatrixB {
    float matrixB[];
};
layout(set = 0, binding = 2) buffer ResultMatrix {
    float resultMatrix[];
};
layout(local_size_x = ${LOCAL_SIZE}, local_size_y = ${LOCAL_SIZE}) in;

void main() {
    uvec2 resultCell = gl_GlobalInvocationID.xy;
    uint resultIndex = resultCell.y + resultCell.x * ${MATRIX_SIZE};

    float result = 0.0f;
    for (uint i = 0; i < ${MATRIX_SIZE}; i++) {
        uint aCell = i + resultCell.x * ${MATRIX_SIZE};
        uint bCell = resultCell.y + i * ${MATRIX_SIZE};
        result += matrixA[aCell] * matrixB[bCell];
    }
    resultMatrix[resultIndex] = result;
}
"""

private data class ComputationResult(val time: Long, val result: FloatArray)

suspend fun runComputeCompareExample(){
    computeSetStatus("MatrixSize", "$MATRIX_SIZE")
    computeSetStatus("Status", "Generating matrices")

    val matrixA = generateMatrix(MATRIX_SIZE * MATRIX_SIZE)
    val matrixB = generateMatrix(MATRIX_SIZE * MATRIX_SIZE)

    computeSetStatus("Status", "Starting CPU calculation")

    val cpuResult = computeCPU(matrixA, matrixB)

    computeSetStatus("CPU Time", "${cpuResult.time} ms")
    computeSetStatus("Status", "Setting up GPU")

    val gpuResult = computeGPU(matrixA, matrixB)
    computeSetStatus("GPU Time", "${gpuResult.time} ms")

    computeSetStatus("Status", "Checking correctness")
    compareResults(cpuResult.result, gpuResult.result)

    computeSetStatus("Status", "Test Completed.")
}

suspend expect fun computeSetStatus(id: String, msg: String)

suspend expect fun timeExecution(func: suspend () -> Unit)  : Long

private suspend fun compareResults(cpuMatrix: FloatArray, gpuMatrix: FloatArray){
    if(cpuMatrix.size != gpuMatrix.size){
        computeSetStatus("Result", "ERROR! Matrices don't have the same size!!!")
        return
    }

    var cpuTotal = cpuMatrix.sum()
    val gpuTotal = gpuMatrix.sum()

    println("CPU: ${cpuTotal}")
    println("GPU: ${gpuTotal}")
    computeSetStatus("Result", "Matrix Difference = ${abs(cpuTotal - gpuTotal)}")
}

private suspend fun computeCPU(matrixA: FloatArray, matrixB: FloatArray) : ComputationResult{
    val output = FloatArray(MATRIX_SIZE * MATRIX_SIZE)

    val time = timeExecution {
        for(resultX in 0 until MATRIX_SIZE){
            for(resultY in 0 until MATRIX_SIZE){
                var sum = 0f 
                
                for(i in 0 until MATRIX_SIZE){
                    val indexA = i + (resultX * MATRIX_SIZE)
                    val indexB = resultY + (i * MATRIX_SIZE)

                    sum += matrixA[indexA] * matrixB[indexB]
                }

                val resultIndex = resultY + (resultX * MATRIX_SIZE)
                output[resultIndex] = sum
            }

            if(resultX % 32 == 0)
                computeSetStatus("Status", "Row $resultX / $MATRIX_SIZE")
        }
    }
    
    computeSetStatus("Status", "Finished CPU Calculation")

    return ComputationResult(time, output)
}

private suspend fun computeGPU(matrixA: FloatArray, matrixB: FloatArray) : ComputationResult{
    val adapter = Kgpu.requestAdapterAsync()
    val device = adapter.requestDeviceAsync()
    val bufferA = BufferUtils.createFloatBuffer(
        device,
        "matrix A buffer",
        matrixA,
        BufferUsage.STORAGE or BufferUsage.COPY_DST or BufferUsage.COPY_SRC
    )
    val bufferB = BufferUtils.createFloatBuffer(
        device,
        "matrix B buffer",
        matrixB,
        BufferUsage.STORAGE or BufferUsage.COPY_DST or BufferUsage.COPY_SRC
    )
    val resultBuffer = device.createBuffer(
        BufferDescriptor(
            "result buffer",
            Primitives.FLOAT_BYTES * MATRIX_SIZE * MATRIX_SIZE,
            BufferUsage.STORAGE or BufferUsage.COPY_DST or BufferUsage.COPY_SRC,
            false
        )
    )

    val bindGroupLayout = device.createBindGroupLayout(
        BindGroupLayoutDescriptor(
            BindGroupLayoutEntry(0, ShaderVisibility.COMPUTE, BindingType.STORAGE_BUFFER),
            BindGroupLayoutEntry(1, ShaderVisibility.COMPUTE, BindingType.STORAGE_BUFFER),
            BindGroupLayoutEntry(2, ShaderVisibility.COMPUTE, BindingType.STORAGE_BUFFER)
        )
    )

    val bindGroup = device.createBindGroup(
        BindGroupDescriptor(
            bindGroupLayout,
            BindGroupEntry(0, bufferA),
            BindGroupEntry(1, bufferB),
            BindGroupEntry(2, resultBuffer)
        )
    )

    val pipelineLayout = device.createPipelineLayout(PipelineLayoutDescriptor(bindGroupLayout))
    val shaderModule = ShaderUtils.fromSource(device, "shader", SHADER_SOURCE, ShaderType.COMPUTE)
    val computePipeline = device.createComputePipeline(
        ComputePipelineDescriptor(
            pipelineLayout,
            ProgrammableStageDescriptor(shaderModule, "main")
        )
    )
    val cmdEncoder = device.createCommandEncoder()
    val computePass = cmdEncoder.beginComputePass()

    computePass.setPipeline(computePipeline)
    computePass.setBindGroup(0, bindGroup)
    computePass.dispatch(MATRIX_SIZE / LOCAL_SIZE, MATRIX_SIZE / LOCAL_SIZE)
    computePass.endPass();

    val readBuffer = device.createBuffer(
        BufferDescriptor(
            "read buffer",
            Primitives.FLOAT_BYTES * MATRIX_SIZE * MATRIX_SIZE,
            BufferUsage.COPY_DST or BufferUsage.MAP_READ,
            false
        )
    )

    computeSetStatus("Status", "Starting GPU calculation")
    var times: FloatArray? = null
    val computationTime = timeExecution {
        cmdEncoder.copyBufferToBuffer(resultBuffer, readBuffer)
        device.getDefaultQueue().submit(cmdEncoder.finish())

        times = ByteUtils.toFloatArray(readBuffer.mapReadAsync(device).getBytes())
    }

    computeSetStatus("Status", "Finished GPU Calculation")
    
    return ComputationResult(computationTime, times!!)
}

fun generateMatrix(size: Int) : FloatArray {
    return FloatArray(size) {
        (Random.nextFloat() - .5f) * 10f
    }
}