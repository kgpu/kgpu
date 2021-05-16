package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h
import jdk.incubator.foreign.MemorySegment

actual interface IntoBindingResource {

    fun intoBindingResource(entries: MemorySegment, index: Long)
}

actual class BindGroupLayoutDescriptor actual constructor(vararg val entries: BindGroupLayoutEntry) {

}

actual class BindGroupEntry actual constructor(val binding: Long, val resource: IntoBindingResource) {

}

actual class BufferBinding actual constructor(val buffer: Buffer, val offset: Long, val size: Long) :
    IntoBindingResource {
    override fun intoBindingResource(entries: MemorySegment, index: Long) {
        wgpu_h.WGPUBindGroupEntry.`buffer$set`(entries, index, buffer.id.address())
        wgpu_h.WGPUBindGroupEntry.`offset$set`(entries, index, offset)
        wgpu_h.WGPUBindGroupEntry.`size$set`(entries, index, size)
    }
}

actual class BindGroupDescriptor
actual constructor(val layout: BindGroupLayout, vararg val entries: BindGroupEntry) {
}

actual class BindGroup(val id: Id) {

    override fun toString(): String {
        return "BindGroup$id"
    }
}


actual abstract class BindingLayout actual constructor() {
    abstract fun intoNative(
        index: Long,
        bufferBinding: MemorySegment,
        samplerBinding: MemorySegment,
        textureBinding: MemorySegment,
        storageTextureBinding: MemorySegment
    )
}

actual class BufferBindingLayout actual constructor(
    val type: BufferBindingType,
    val hasDynamicOffset: Boolean,
    val minBindingSize: Long
) : BindingLayout() {

    override fun intoNative(
        index: Long,
        bufferBinding: MemorySegment,
        samplerBinding: MemorySegment,
        textureBinding: MemorySegment,
        storageTextureBinding: MemorySegment
    ) {
        wgpu_h.WGPUBufferBindingLayout.`type$set`(bufferBinding, index, type.nativeVal)
        wgpu_h.WGPUBufferBindingLayout.`hasDynamicOffset$set`(bufferBinding, index, hasDynamicOffset.toNativeByte())
        wgpu_h.WGPUBufferBindingLayout.`minBindingSize$set`(bufferBinding, index, minBindingSize)
    }
}

actual class BindGroupLayoutEntry actual constructor(
    val binding: Long,
    val visibility: Long,
    val bindingLayout: BindingLayout
)

actual class BindGroupLayout internal constructor(val id: Id) {

    override fun toString(): String {
        return "BindGroupLayout$id"
    }
}

actual class PipelineLayoutDescriptor internal constructor(val ids: LongArray) {

    actual constructor(vararg bindGroupLayouts: BindGroupLayout) : this(bindGroupLayouts.map { it.id.id }.toLongArray())
}

actual class PipelineLayout(val id: Id) {

    override fun toString(): String {
        return "PipelineLayout$id"
    }
}