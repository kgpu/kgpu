package io.github.kgpu.kcgmath

import kotlin.math.*

data class Vec3(var x: Float, var y: Float, var z: Float) {

    constructor() : this(0f, 0f, 0f)
    constructor(x: Float, y: Float) : this(x, y, 0f)

    companion object {
        val UNIT_X = Vec3(1f, 0f, 0f)
        val UNIT_Y = Vec3(0f, 1f, 0f)
        val UNIT_Z = Vec3(0f, 0f, 1f)
        val ZERO = Vec3(0f, 0f, 0f)
        val ONE = Vec3(1f, 1f, 1f)
    }

    fun mul(scalar: Float): Vec3 {
        x *= scalar
        y *= scalar
        z *= scalar

        return this
    }

    fun mul(other: Vec3): Vec3 {
        x *= other.x
        y *= other.y
        z *= other.z

        return this
    }

    fun normalize(): Vec3 {
        var length = length()

        if (length > 0) {
            length = 1 / length
        }

        x *= length
        y *= length
        z *= length

        return this
    }

    fun angle(other: Vec3): Float {
        val mag = this.length() * other.length()
        return if (mag != 0f) {
            val cosTheta = dot(other) / mag

            acos(cosTheta.coerceIn(-1f, 1f))
        } else {
            0f
        }
    }

    fun dot(other: Vec3): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    fun add(other: Vec3): Vec3 {
        x += other.x
        y += other.y
        z += other.z

        return this
    }

    fun add(x: Float, y: Float, z: Float): Vec3 {
        this.x += x
        this.y += y
        this.z += z

        return this
    }

    fun sub(other: Vec3): Vec3 {
        x -= other.x
        y -= other.y
        z -= other.z

        return this
    }

    fun sub(x: Float, y: Float, z: Float): Vec3 {
        this.x -= x
        this.y -= y
        this.z -= z

        return this
    }

    fun length(): Float {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Float {
        return x.pow(2) + y.pow(2) + z.pow(2)
    }

    fun distance(other: Vec3): Float {
        return sqrt(distanceSquared(other))
    }

    fun distanceSquared(other: Vec3): Float {
        val x = other.x - x
        val y = other.y - y
        val z = other.z - z

        return x.pow(2) + y.pow(2) + z.pow(2)
    }
}
