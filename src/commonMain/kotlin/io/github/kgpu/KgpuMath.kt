package io.github.kgpu

expect class Matrix4f(){

    fun toFloats() : FloatArray

    fun translate(x: Float, y: Float, z: Float) : Matrix4f
}