package io.github.kgpu

import org.joml.Vector3f
import org.joml.Vector3fc

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
        mat.lookAt(eye.intoJoml(), center.intoJoml(), up.intoJoml())

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

fun Vec3f.intoJoml() : Vector3fc{
    return Vector3f(this.x, this.y, this.z)
}