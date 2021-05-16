package io.github.kgpu

actual class CommandBuffer(val jsType: GPUCommandBuffer)

external class GPUCommandBuffer

actual class TextureCopyView
actual constructor(texture: Texture, val mipLevel: Long, val origin: Origin3D) {
    val texture = texture.jsType
}

actual class BufferCopyView
actual constructor(
    buffer: Buffer, val bytesPerRow: Int, val rowsPerImage: Int, val offset: Long
) {

    val buffer = buffer.jsType
}

actual class CommandEncoder(val jsType: GPUCommandEncoder) {

    actual fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder {
        return RenderPassEncoder(jsType.beginRenderPass(desc))
    }

    actual fun finish(): CommandBuffer {
        return CommandBuffer(jsType.finish())
    }

    actual fun copyBufferToTexture(
        source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D
    ) {
        jsType.copyBufferToTexture(source, destination, copySize)
    }

    actual fun beginComputePass(): ComputePassEncoder {
        return ComputePassEncoder(jsType.beginComputePass())
    }

    actual fun copyBufferToBuffer(
        source: Buffer, destination: Buffer, size: Long, sourceOffset: Int, destinationOffset: Int
    ) {
        jsType.copyBufferToBuffer(
            source.jsType, sourceOffset, destination.jsType, destinationOffset, size
        )
    }

    actual fun copyTextureToBuffer(source: TextureCopyView, dest: BufferCopyView, size: Extent3D) {
        jsType.copyTextureToBuffer(source, dest, size)
    }
}

external class GPUCommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): GPURenderPassEncoder

    fun finish(): GPUCommandBuffer

    fun copyBufferToTexture(
        source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D
    )

    fun beginComputePass(): GPUComputePassEncoder

    fun copyBufferToBuffer(
        source: GPUBuffer,
        sourceOffset: Int,
        destination: GPUBuffer,
        destinationOffset: Int,
        size: Long
    )

    fun copyTextureToBuffer(
        source: TextureCopyView, destination: BufferCopyView, copySize: Extent3D
    )
}

actual class RenderPassEncoder(val jsType: GPURenderPassEncoder) {
    actual fun setPipeline(pipeline: RenderPipeline) {
        jsType.setPipeline(pipeline)
    }

    actual fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        jsType.draw(vertexCount, instanceCount, firstVertex, firstInstance)
    }

    actual fun endPass() {
        jsType.endPass()
    }

    actual fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long, size: Long) {
        jsType.setVertexBuffer(slot, buffer.jsType, offset, size)
    }

    actual fun drawIndexed(
        indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int
    ) {
        jsType.drawIndexed(indexCount, instanceCount, firstVertex, baseVertex, firstInstance)
    }

    actual fun setIndexBuffer(buffer: Buffer, indexFormat: IndexFormat, offset: Long, size: Long) {
        jsType.setIndexBuffer(buffer.jsType, indexFormat.jsType, offset, size)
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        jsType.setBindGroup(index, bindGroup.jsType)
    }
}

external class GPURenderPassEncoder {
    fun setPipeline(pipeline: RenderPipeline)

    fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int)

    fun endPass()

    fun setVertexBuffer(slot: Long, buffer: GPUBuffer, offset: Long, size: Long)

    fun drawIndexed(
        indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int
    )

    fun setIndexBuffer(buffer: GPUBuffer, format: String?, offset: Long, size: Long)

    fun setBindGroup(index: Int, bindGroup: GPUBindGroup)
}

actual class RenderPassDescriptor
actual constructor(vararg val colorAttachments: RenderPassColorAttachmentDescriptor)

actual class ComputePipeline

actual class RenderPassColorAttachmentDescriptor actual constructor(
    attachment: TextureView,
    loadOp: LoadOp,
    storeOp: StoreOp,
    clearColor: Color?,
    resolveTarget: TextureView?
) {
    val view = attachment.jsType
    val loadValue = clearColor ?: loadOp.jsType
    val storeOp = storeOp.jsType
    val resolveTarget = resolveTarget?.jsType ?: undefined
}

actual class RenderPipeline

actual class ProgrammableStageDescriptor
actual constructor(val module: ShaderModule, val entryPoint: String)

actual class BlendComponent
actual constructor(srcFactor: BlendFactor, dstFactor: BlendFactor, operation: BlendOperation) {

    val operation = operation.jsType
    val srcFactor = srcFactor.jsType
    val dstFactor = dstFactor.jsType
}

actual class FragmentState actual constructor(
    val module: ShaderModule,
    val entryPoint: String,
    val targets: Array<ColorTargetState>
)

actual class BlendState actual constructor(color: BlendComponent, alpha: BlendComponent)
actual class RenderPipelineDescriptor actual constructor(
    val layout: PipelineLayout,
    val vertex: VertexState,
    val primitive: PrimitiveState,
    depthStencil: Any?,
    val multisample: MultisampleState,
    val fragment: FragmentState?
)

actual class MultisampleState actual constructor(
    val count: Int,
    val mask: Int,
    val alphaToCoverageEnabled: Boolean
)

actual class VertexState actual constructor(
    val module: ShaderModule,
    val entryPoint: String,
    vararg val buffers: VertexBufferLayout
)

actual class PrimitiveState actual constructor(
    topology: PrimitiveTopology,
    stripIndexFormat: IndexFormat?,
    frontFace: FrontFace,
    cullMode: CullMode
) {
    val topology = topology.jsType
    val stripIndexFormat = stripIndexFormat?.jsType ?: undefined
    val frontFace = frontFace.jsType
    val cullMode = cullMode.jsType
}

actual class ColorTargetState actual constructor(
    format: TextureFormat,
    val blendState: BlendState?,
    val writeMask: Long
) {
    val format = format.jsType
}

actual class VertexAttribute actual constructor(
    format: VertexFormat,
    val offset: Long,
    val shaderLocation: Int
) {
    val format = format.jsType
}

actual class VertexBufferLayout actual constructor(
    arrayStride: Long,
    stepMode: InputStepMode,
    vararg attributes: VertexAttribute
)

actual class ComputePipelineDescriptor
actual constructor(val layout: PipelineLayout, computeStage: ProgrammableStageDescriptor) {
    val compute = computeStage
}

actual class ComputePassEncoder(val jsType: GPUComputePassEncoder) {

    actual fun setPipeline(pipeline: ComputePipeline) {
        jsType.setPipeline(pipeline)
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        jsType.setBindGroup(index, bindGroup.jsType)
    }

    actual fun dispatch(x: Int, y: Int, z: Int) {
        jsType.dispatch(x, y, z)
    }

    actual fun endPass() {
        jsType.endPass()
    }
}

external class GPUComputePassEncoder {
    fun setPipeline(pipeline: ComputePipeline)

    fun setBindGroup(index: Int, bindGroup: GPUBindGroup)

    fun dispatch(x: Int, y: Int, z: Int)

    fun endPass()
}