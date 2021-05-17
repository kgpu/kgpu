package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h.*
import jdk.incubator.foreign.MemorySegment

actual interface IntoBindingResource {

    fun intoBindingResource(entry: MemorySegment)
}

actual class BindGroupLayoutDescriptor actual constructor(vararg val entries: BindGroupLayoutEntry) {

}

actual class BindGroupEntry actual constructor(val binding: Long, val resource: IntoBindingResource) {

}

actual class BufferBinding actual constructor(val buffer: Buffer, val offset: Long, val size: Long) :
    IntoBindingResource {
    override fun intoBindingResource(entry: MemorySegment) {
        WGPUBindGroupEntry.`buffer$set`(entry, buffer.id.address())
        WGPUBindGroupEntry.`offset$set`(entry, offset)
        WGPUBindGroupEntry.`size$set`(entry, size)
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
        bufferBinding: MemorySegment,
        samplerBinding: MemorySegment,
        textureBinding: MemorySegment,
        storageTextureBinding: MemorySegment
    ) {
        WGPUBufferBindingLayout.`type$set`(bufferBinding, type.nativeVal)
        WGPUBufferBindingLayout.`hasDynamicOffset$set`(bufferBinding, hasDynamicOffset.toNativeByte())
        WGPUBufferBindingLayout.`minBindingSize$set`(bufferBinding, minBindingSize)
    }
}

actual class TextureBindingLayout actual constructor(
    val sampleType: TextureSampleType,
    val viewDimension: TextureViewDimension,
    val multisampled: Boolean,
) : BindingLayout() {
    override fun intoNative(
        bufferBinding: MemorySegment,
        samplerBinding: MemorySegment,
        textureBinding: MemorySegment,
        storageTextureBinding: MemorySegment
    ) {
        WGPUTextureBindingLayout.`sampleType$set`(textureBinding, sampleType.nativeVal)
        WGPUTextureBindingLayout.`viewDimension$set`(textureBinding, viewDimension.nativeVal)
        WGPUTextureBindingLayout.`multisampled$set`(textureBinding, multisampled.toNativeByte())
    }

}

actual class SamplerBindingLayout actual constructor(
    val type: SamplerBindingType
) : BindingLayout() {
    override fun intoNative(
        bufferBinding: MemorySegment,
        samplerBinding: MemorySegment,
        textureBinding: MemorySegment,
        storageTextureBinding: MemorySegment
    ) {
        WGPUSamplerBindingLayout.`type$set`(samplerBinding, type.nativeVal)
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