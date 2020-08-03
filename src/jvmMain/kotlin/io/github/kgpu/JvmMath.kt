package io.github.kgpu

import org.joml.Vector3f

actual class Matrix4f constructor(val mat: org.joml.Matrix4f) {

    actual constructor() : this(org.joml.Matrix4f())

    actual constructor(original: Matrix4f) : this(org.joml.Matrix4f(original.mat))

    actual fun toFloats(): FloatArray {
        return mat.get(FloatArray(16))
    }

    actual fun toBytes(): ByteArray {
        return ByteUtils.toByteArray(toFloats())
    }

    actual fun translate(x: Float, y: Float, z: Float): Matrix4f {
        mat.translate(x, y, z)

        return this
    }

    actual fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4f {
        mat.ortho(left, right, bottom, top, near, far)

        return this
    }

    actual fun lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Matrix4f {
        mat.lookAt(eye.vec, center.vec, up.vec)

        return this
    }

    actual fun mul(other: Matrix4f): Matrix4f {
        mat.mul(other.mat)

        return this
    }

    actual fun perspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix4f {
        mat.perspective(fov, aspect, near, far)

        return this
    }

    actual fun rotateX(angle: Float): Matrix4f {
        mat.rotateX(angle)

        return this
    }

    actual fun rotateY(angle: Float): Matrix4f {
        mat.rotateY(angle)

        return this
    }

    actual fun rotateZ(angle: Float): Matrix4f {
        mat.rotateZ(angle)

        return this
    }

    actual fun invert(): Matrix4f {
        mat.invert()

        return this
    }

    actual fun transpose(): Matrix4f {
        mat.transpose()

        return this
    }
}

actual class Vec3f actual constructor(x: Float, y: Float, z: Float) {
    val vec = Vector3f(x, y, z)

    actual constructor() : this(0f, 0f, 0f)

    actual var x: Float
        get() = vec.x
        set(value) {
            vec.x = value
        }
    actual var y: Float
        get() = vec.y
        set(value) {
            vec.y = value
        }
    actual var z: Float
        get() = vec.z
        set(value) {
            vec.z = value
        }

    actual fun mul(scalar: Float): Vec3f {
        vec.mul(scalar)

        return this
    }

    actual fun normalize(): Vec3f {
        vec.normalize()

        return this
    }

    override fun toString(): String {
        return "Vec3f($x, $y, $z)"
    }

}