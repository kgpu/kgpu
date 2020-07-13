package io.github.kgpu

import io.github.kgpu.internal.mat4
import io.github.kgpu.internal.vec3
import org.khronos.webgl.Float32Array
import org.khronos.webgl.get

actual class Matrix4f actual constructor(){
    val mat = mat4.create()

    actual fun translate(x: Float, y: Float, z: Float): Matrix4f {
        val xyz = vec3.fromValues(x, y, z)
        mat4.translate(mat, mat, xyz)

        return this
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    actual fun toFloats(): FloatArray {
        val out = FloatArray(16)
        val mat = mat as Float32Array

        for(i in 0..16){
            out[i] = mat[i]
        }

        return out
    }
}