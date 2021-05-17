package io.github.kgpu

/**
 * Represents something that is a binding resource. Examples include buffer, samplers, and texture
 * views.
 *
 * __See:__ [Binding Resource Spec](https://gpuweb.github.io/gpuweb/#typedefdef-gpubindingresource)
 */
expect interface IntoBindingResource

expect class BindGroupEntry(binding: Long, resource: IntoBindingResource)

expect class BindGroupDescriptor(layout: BindGroupLayout, vararg entries: BindGroupEntry)

expect class BindGroup

expect class BufferBinding(buffer: Buffer, offset: Long = 0, size: Long = buffer.size - offset) : IntoBindingResource

expect abstract class BindingLayout()

expect class BufferBindingLayout(
    type: BufferBindingType = BufferBindingType.UNIFORM,
    hasDynamicOffset: Boolean = false,
    minBindingSize: Long = 0,
) : BindingLayout

expect class TextureBindingLayout(
    sampleType: TextureSampleType = TextureSampleType.FLOAT,
    viewDimension: TextureViewDimension = TextureViewDimension.D2,
    multisampled: Boolean = false,
) : BindingLayout

expect class SamplerBindingLayout(
    type: SamplerBindingType = SamplerBindingType.FILTERING
) : BindingLayout

// TODO: Implement these
//class StorageTextureBindingLayout() : BindingLayout()
//class ExternalTextureBindingLayout() : BindingLayout()

expect class BindGroupLayoutEntry(
    binding: Long,
    visibility: Long,
    bindingLayout: BindingLayout)

expect class BindGroupLayoutDescriptor(vararg entries: BindGroupLayoutEntry)

expect class BindGroupLayout

expect class PipelineLayout

expect class PipelineLayoutDescriptor(vararg bindGroupLayouts: BindGroupLayout)