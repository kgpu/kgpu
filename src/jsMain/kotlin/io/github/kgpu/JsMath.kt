package io.github.kgpu

import io.github.kgpu.internal.Mat4
import io.github.kgpu.internal.Vec3
import io.github.kgpu.internal.mat4
import io.github.kgpu.internal.vec3
import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set

actual class Matrix4f constructor(val mat : Mat4) {

    actual constructor() : this(mat4.create())

    actual constructor(original: Matrix4f) : this(mat4.clone(original.mat))

    actual fun translate(x: Float, y: Float, z: Float): Matrix4f {
        val xyz = vec3.fromValues(x, y, z)
        mat4.translate(mat, mat, xyz)

        return this
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    actual fun toFloats(): FloatArray {
        val out = FloatArray(16)
        val mat = mat as Float32Array

        for (i in 0..16) {
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
        mat4.lookAt(mat, eye.intoJsType(), center.intoJsType(), up.intoJsType())

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

    actual fun invert(): Matrix4f {
        mat4.invert(mat, mat)

        return this
    }

    actual fun transpose(): Matrix4f {
        mat4.transpose(mat, mat)

        return this
    }
}

fun Vec3f.intoJsType() : Vec3 {
    return vec3.fromValues(this.x, this.y, this.z)
}