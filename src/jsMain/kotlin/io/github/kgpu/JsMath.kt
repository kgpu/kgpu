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

    actual fun toBytes(): ByteArray {
        return ByteUtils.toByteArray(toFloats())
    }

    actual fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4f {
        mat4.ortho(mat, left, right, bottom, top, near, far)

        return this
    }

    actual fun lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Matrix4f {
        mat4.lookAt(mat, eye.vec, center.vec, up.vec)

        return this
    }

    actual fun mul(other: Matrix4f): Matrix4f {
        mat4.mul(mat, mat, other.mat)

        return this
    }

    actual fun perspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix4f {
        mat4.perspective(mat, fov, aspect, near, far)

        return this
    }

    actual fun rotateX(angle: Float): Matrix4f {
        mat4.rotateX(mat, mat, angle)

        return this
    }

    actual fun rotateY(angle: Float): Matrix4f {
        mat4.rotateY(mat, mat, angle)

        return this
    }

    actual fun rotateZ(angle: Float): Matrix4f {
        mat4.rotateZ(mat, mat, angle)

        return this
    }
}

actual class Vec3f actual constructor(x: Float, y: Float, z: Float){
    val vec = vec3.fromValues(x, y, z)

    actual constructor() : this(0f, 0f, 0f)

}