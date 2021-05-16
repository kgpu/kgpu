package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h
import jdk.incubator.foreign.MemoryAddress
import jdk.incubator.foreign.NativeScope

actual class CommandBuffer(val id: Id) {

    override fun toString(): String {
        return "CommandBuffer$id"
    }
}

actual class TextureCopyView
actual constructor(texture: Texture, mipLevel: Long, origin: Origin3D) {
}

actual class BufferCopyView
actual constructor(buffer: Buffer, bytesPerRow: Int, rowsPerImage: Int, offset: Long) {

}

actual class CommandEncoder(val id: Id) {

    override fun toString(): String {
        return "CommandEncoder$id"
    }

    actual fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder {
        return RenderPassEncoder(NativeScope.unboundedScope().use { scope ->
            val descriptor = wgpu_h.WGPURenderPassDescriptor.allocate(scope)
            val colorAttachments =
                wgpu_h.WGPURenderPassColorAttachmentDescriptor.allocateArray(desc.colorAttachments.size, scope)
            val colors = wgpu_h.WGPURenderPassColorAttachmentDescriptor.`clearColor$slice`(colorAttachments)

            desc.colorAttachments.forEachIndexed { indexInt, attachment ->
                val index = indexInt.toLong()
                wgpu_h.WGPURenderPassColorAttachmentDescriptor.`attachment$set`(
                    colorAttachments,
                    index,
                    attachment.attachment.id.address()
                )
                wgpu_h.WGPURenderPassColorAttachmentDescriptor.`resolveTarget$set`(
                    colorAttachments,
                    index,
                    attachment.resolveTarget?.id?.address() ?: CUtils.NULL
                )
                wgpu_h.WGPURenderPassColorAttachmentDescriptor.`loadOp$set`(
                    colorAttachments,
                    index,
                    attachment.loadOp.nativeVal,
                )
                wgpu_h.WGPURenderPassColorAttachmentDescriptor.`storeOp$set`(
                    colorAttachments,
                    index,
                    attachment.storeOp.nativeVal
                )
                wgpu_h.WGPUColor.`r$set`(colors, index, attachment.clearColor?.r ?: 0.0)
                wgpu_h.WGPUColor.`g$set`(colors, index, attachment.clearColor?.g ?: 0.0)
                wgpu_h.WGPUColor.`b$set`(colors, index, attachment.clearColor?.b ?: 0.0)
                wgpu_h.WGPUColor.`a$set`(colors, index, attachment.clearColor?.a ?: 0.0)
            }

            wgpu_h.WGPURenderPassDescriptor.`colorAttachments$set`(descriptor, colorAttachments.address())
            wgpu_h.WGPURenderPassDescriptor.`colorAttachmentCount$set`(descriptor, desc.colorAttachments.size)

            wgpu_h.wgpuCommandEncoderBeginRenderPass(id, descriptor.address())
        })
    }

    actual fun finish(): CommandBuffer {
        return CommandBuffer(Id(NativeScope.unboundedScope().use { scope ->
            val descriptor = wgpu_h.WGPUCommandBufferDescriptor.allocate(scope)
            //TODO: Support labels
            wgpu_h.WGPUCommandBufferDescriptor.`label$set`(descriptor, CUtils.NULL)
            wgpu_h.wgpuCommandEncoderFinish(id, descriptor)
        }))
    }

    actual fun copyBufferToTexture(
        source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D
    ) {
        TODO()
    }

    actual fun beginComputePass(): ComputePassEncoder {
        return ComputePassEncoder(NativeScope.unboundedScope().use { scope ->
            val descriptor = wgpu_h.WGPUComputePassDescriptor.allocate(scope)
            wgpu_h.WGPUComputePassDescriptor.`label$set`(descriptor, CUtils.NULL)

            wgpu_h.wgpuCommandEncoderBeginComputePass(id, descriptor.address())
        })
    }

    actual fun copyBufferToBuffer(
        source: Buffer, destination: Buffer, size: Long, sourceOffset: Int, destinationOffset: Int
    ) {
        wgpu_h.wgpuCommandEncoderCopyBufferToBuffer(
            id,
            source.id.address(),
            sourceOffset.toLong(),
            destination.id.address(),
            destinationOffset.toLong(),
            size
        )
    }

    actual fun copyTextureToBuffer(source: TextureCopyView, dest: BufferCopyView, size: Extent3D) {
        TODO()
    }
}

actual class RenderPassEncoder(var pass: MemoryAddress) {

    override fun toString(): String {
        return "RenderPassEncoder"
    }

    actual fun setPipeline(pipeline: RenderPipeline) {
        assertPassStillValid()
        wgpu_h.wgpuRenderPassEncoderSetPipeline(pass, pipeline.id)
    }

    actual fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        assertPassStillValid()
        wgpu_h.wgpuRenderPassEncoderDraw(pass, vertexCount, instanceCount, firstVertex, firstInstance)
    }

    actual fun endPass() {
        wgpu_h.wgpuRenderPassEncoderEndPass(pass)
        pass = CUtils.NULL
    }

    actual fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long, size: Long) {
        assertPassStillValid()
        wgpu_h.wgpuRenderPassEncoderSetVertexBuffer(pass, slot.toInt(), buffer.id, offset, size)
    }

    actual fun drawIndexed(
        indexCount: Int, instanceCount: Int, firstVertex: Int, baseVertex: Int, firstInstance: Int
    ) {
        assertPassStillValid()
        TODO()
    }

    actual fun setIndexBuffer(buffer: Buffer, indexFormat: IndexFormat, offset: Long, size: Long) {
        assertPassStillValid()
        TODO()
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        assertPassStillValid()
        TODO()
    }

    private fun assertPassStillValid() {
        if (pass == CUtils.NULL)
            throw RuntimeException("Render Pass Encoder has ended.")
    }
}

actual class RenderPassColorAttachmentDescriptor
actual constructor(
    val attachment: TextureView,
    val loadOp: LoadOp,
    val storeOp: StoreOp,
    val clearColor: Color?,
    val resolveTarget: TextureView?,
)

actual class RenderPassDescriptor
actual constructor(vararg val colorAttachments: RenderPassColorAttachmentDescriptor) {
}

actual class RenderPipeline internal constructor(val id: Id) {

    override fun toString(): String {
        return "RenderPipeline$id"
    }
}

actual class RenderPipelineDescriptor actual constructor(
    val layout: PipelineLayout,
    val vertex: VertexState,
    val primitive: PrimitiveState,
    val depthStencil: Any?,
    val multisample: MultisampleState,
    val fragment: FragmentState?
)

actual class ProgrammableStageDescriptor
actual constructor(val module: ShaderModule, val entryPoint: String)

actual class FragmentState actual constructor(
    val module: ShaderModule,
    val entryPoint: String,
    val targets: Array<ColorTargetState>
)

actual class BlendState actual constructor(val color: BlendComponent, val alpha: BlendComponent)

actual class BlendComponent
actual constructor(
    val srcFactor: BlendFactor, val dstFactor: BlendFactor, val operation: BlendOperation
)

actual class ColorTargetState actual constructor(
    val format: TextureFormat,
    val blendState: BlendState?,
    val writeMask: Long
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
    val topology: PrimitiveTopology,
    val stripIndexFormat: IndexFormat?,
    val frontFace: FrontFace,
    val cullMode: CullMode
)

actual class VertexAttribute actual constructor(
    val format: VertexFormat,
    val offset: Long,
    val shaderLocation: Int
)

actual class VertexBufferLayout actual constructor(
    val arrayStride: Long,
    val stepMode: InputStepMode,
    vararg attributes: VertexAttribute
)

actual class ComputePipelineDescriptor
actual constructor(val layout: PipelineLayout, val computeStage: ProgrammableStageDescriptor) {
}

actual class ComputePipeline internal constructor(val id: Id) {

    override fun toString(): String {
        return "ComputePipeline$id"
    }
}

actual class ComputePassEncoder(var pass: MemoryAddress) {

    override fun toString(): String {
        return "ComputePassEncoder"
    }

    actual fun setPipeline(pipeline: ComputePipeline) {
        assertPassStillValid()

        wgpu_h.wgpuComputePassEncoderSetPipeline(pass, pipeline.id.address())
    }

    actual fun setBindGroup(index: Int, bindGroup: BindGroup) {
        assertPassStillValid()

        wgpu_h.wgpuComputePassEncoderSetBindGroup(pass, index, bindGroup.id.address(), 0, CUtils.NULL)
    }

    actual fun dispatch(x: Int, y: Int, z: Int) {
        assertPassStillValid()

        wgpu_h.wgpuComputePassEncoderDispatch(pass, x, y, z)
    }

    actual fun endPass() {
        assertPassStillValid()

        wgpu_h.wgpuComputePassEncoderEndPass(pass)
        pass = CUtils.NULL
    }

    private fun assertPassStillValid() {
        if (pass == CUtils.NULL)
            throw RuntimeException("Compute Pass Encoder has ended.")
    }
}