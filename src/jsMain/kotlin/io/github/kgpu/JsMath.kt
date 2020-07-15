package io.github.kgpu

import io.github.kgpu.internal.Mat4
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

    actual fun invert(): Matrix4f {
        mat4.invert(mat, mat)

        return this
    }

    actual fun transpose(): Matrix4f {
        mat4.transpose(mat, mat)

        return this
    }
}

actual class Vec3f actual constructor(x: Float, y: Float, z: Float) {
    val vec = vec3.fromValues(x, y, z)

    actual constructor() : this(0f, 0f, 0f)

    actual var x: Float
        get() = toArrayType(vec)[0]
        set(value) {
            toArrayType(vec)[0] = value
        }
    actual var y: Float
        get() = toArrayType(vec)[1]
        set(value) {
            toArrayType(vec)[1] = value
        }
    actual var z: Float
        get() = toArrayType(vec)[2]
        set(value) {
            toArrayType(vec)[2] = value
        }

    actual fun mul(scalar: Float): Vec3f {
        vec3.mul(vec, vec, vec3.fromValues(scalar, scalar, scalar))

        return this
    }

    actual fun normalize(): Vec3f {
        vec3.normalize(vec, vec)

        return this
    }

}

private fun toArrayType(input: dynamic): Float32Array {
    return input as Float32Array
}