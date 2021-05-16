package io.github.kgpu

expect class CommandBuffer

expect class TextureCopyView(
    texture: Texture, mipLevel: Long = 0, origin: Origin3D = Origin3D(0, 0, 0)
)

expect class BufferCopyView(
    buffer: Buffer, bytesPerRow: Int, rowsPerImage: Int, offset: Long = 0
)

expect class CommandEncoder {

    fun beginRenderPass(desc: RenderPassDescriptor): RenderPassEncoder

    fun finish(): CommandBuffer

    fun copyBufferToTexture(
        source: BufferCopyView, destination: TextureCopyView, copySize: Extent3D
    )

    fun beginComputePass(): ComputePassEncoder

    fun copyBufferToBuffer(
        source: Buffer,
        destination: Buffer,
        size: Long = destination.size,
        sourceOffset: Int = 0,
        destinationOffset: Int = 0
    )

    fun copyTextureToBuffer(source: TextureCopyView, dest: BufferCopyView, size: Extent3D)
}

expect class ProgrammableStageDescriptor(module: ShaderModule, entryPoint: String)

expect class RenderPipelineDescriptor(
    layout: PipelineLayout,
    vertex: VertexState,
    primitive: PrimitiveState,
    depthStencil: Any?,
    multisample: MultisampleState,
    fragment: FragmentState?
)

expect class MultisampleState(
    count: Int,
    mask: Int,
    alphaToCoverageEnabled: Boolean,
)

expect class VertexState(
    module: ShaderModule,
    entryPoint: String,
    vararg buffers: VertexBufferLayout
)

expect class PrimitiveState(
    topology: PrimitiveTopology,
    stripIndexFormat: IndexFormat? = null,
    frontFace: FrontFace = FrontFace.CCW,
    cullMode: CullMode = CullMode.NONE,
)

expect class FragmentState(
    module: ShaderModule,
    entryPoint: String,
    targets: Array<ColorTargetState>
)

expect class ColorTargetState(
    format: TextureFormat,
    blendState: BlendState?,
    writeMask: Long
)

expect class BlendState(color: BlendComponent, alpha: BlendComponent)

expect class VertexAttribute(format: VertexFormat, offset: Long, shaderLocation: Int)

expect class VertexBufferLayout(
    arrayStride: Long, stepMode: InputStepMode, vararg attributes: VertexAttribute
)

expect class BlendComponent(
    srcFactor: BlendFactor = BlendFactor.ONE,
    dstFactor: BlendFactor = BlendFactor.ZERO,
    operation: BlendOperation = BlendOperation.ADD
)

expect class RenderPipeline

expect class RenderPassEncoder {

    fun setPipeline(pipeline: RenderPipeline)

    fun draw(vertexCount: Int, instanceCount: Int, firstVertex: Int = 0, firstInstance: Int = 0)

    fun endPass()

    fun setVertexBuffer(slot: Long, buffer: Buffer, offset: Long = 0, size: Long = buffer.size)

    fun drawIndexed(
        indexCount: Int,
        instanceCount: Int,
        firstVertex: Int = 0,
        baseVertex: Int = 0,
        firstInstance: Int = 0
    )

    fun setIndexBuffer(
        buffer: Buffer, indexFormat: IndexFormat, offset: Long = 0, size: Long = buffer.size
    )

    fun setBindGroup(index: Int, bindGroup: BindGroup)
}

expect class RenderPassColorAttachmentDescriptor(
    attachment: TextureView,
    loadOp: LoadOp,
    storeOp: StoreOp,
    clearColor: Color? = null,
    resolveTarget: TextureView? = null,
)

expect class RenderPassDescriptor(vararg colorAttachments: RenderPassColorAttachmentDescriptor)

expect class ComputePipelineDescriptor(
    layout: PipelineLayout, computeStage: ProgrammableStageDescriptor
)

expect class ComputePipeline

expect class ComputePassEncoder {

    fun setPipeline(pipeline: ComputePipeline)

    fun setBindGroup(index: Int, bindGroup: BindGroup)

    fun dispatch(x: Int, y: Int = 1, z: Int = 1)

    fun endPass()
}