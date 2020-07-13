package io.github.kgpu

actual class Matrix4f actual constructor(){
    val mat = org.joml.Matrix4f()

    actual fun toFloats(): FloatArray {
        return mat.get(FloatArray(16))
    }

    actual fun translate(x: Float, y: Float, z: Float): Matrix4f {
        mat.translate(x, y, z)

        return this
    }
}