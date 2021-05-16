package io.github.kgpu

actual interface IntoBindingResource {

    fun intoBindingResource(): dynamic
}

actual class BufferBinding actual constructor(buffer: Buffer, val offset: Long, val size: Long) :
    IntoBindingResource {

    val buffer = buffer.jsType

    override fun intoBindingResource(): dynamic {
        return this
    }
}

actual class BindGroupEntry actual constructor(val binding: Long, resource: IntoBindingResource) {

    val resource = resource.intoBindingResource()
}

actual class BindGroupDescriptor
actual constructor(layout: BindGroupLayout, vararg val entries: BindGroupEntry) {
    val layout = layout.jsType
}

actual class BindGroup(val jsType: GPUBindGroup)

external class GPUBindGroup

actual abstract class BindingLayout actual constructor() {
    abstract fun toJsType(entry: dynamic)
}

actual class BufferBindingLayout actual constructor(
    type: BufferBindingType,
    val hasDynamicOffset: Boolean,
    val minBindingSize: Long
) : BindingLayout() {
    val type = type.jsType

    override fun toJsType(entry: dynamic) {
        entry.buffer = this
    }
}

actual class BindGroupLayoutDescriptor
actual constructor(vararg entries: BindGroupLayoutEntry) {
    val entries: Array<dynamic> = entries.map { it.jsType }.toTypedArray()
}

actual class BindGroupLayoutEntry actual constructor(
    binding: Long,
    visibility: Long,
    bindingLayout: BindingLayout) {

    // TODO: All asDynamic() should be changed to this
    val jsType = Any().asDynamic()

    init {
        jsType.binding = binding
        jsType.visibility = visibility
        bindingLayout.toJsType(jsType)
    }

}

actual class BindGroupLayout(val jsType: GPUBindGroupLayout) {}

external class GPUBindGroupLayout {}

actual class PipelineLayoutDescriptor actual constructor(vararg bindGroupLayouts: BindGroupLayout) {
    val bindGroupLayouts = bindGroupLayouts.map { it.jsType }.toTypedArray()
}

actual typealias PipelineLayout = GPUPipelineLayout

external class GPUPipelineLayout
