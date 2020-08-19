package io.github.kgpu

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

data class Point(val x: Int, val y: Int)

object MathUtils{

    val UNIT_X = Vec3f(1f, 0f, 0f)
    val UNIT_Y = Vec3f(0f, 1f, 0f)
    val UNIT_Z = Vec3f(0f, 0f, 1f)
    val VEC_ONE = Vec3f(1f, 1f, 1f)
    const val PIf = PI.toFloat()

    fun toRadians(degrees: Float) : Float{
        return degrees * PIf / 180f
    }
}

expect class Matrix4f(){

    constructor(original: Matrix4f)

    fun toFloats() : FloatArray

    fun toBytes() : ByteArray

    fun translate(x: Float, y: Float, z: Float) : Matrix4f

    fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) : Matrix4f

    fun lookAt(eye: Vec3f, center: Vec3f, up: Vec3f) : Matrix4f

    fun mul(other: Matrix4f) : Matrix4f

    fun rotateX(angle: Float) : Matrix4f

    fun rotateY(angle: Float) : Matrix4f

    fun rotateZ(angle: Float) : Matrix4f

    fun perspective(fov: Float, aspect: Float, near: Float, far: Float) : Matrix4f

    fun invert() : Matrix4f

    fun transpose() : Matrix4f
}

data class Vec3f(var x: Float, var y: Float, var z: Float){

    constructor() : this(0f, 0f, 0f)
    constructor(x: Float, y: Float) : this(x, y, 0f)

    fun mul(scalar: Float) : Vec3f{
        x *= scalar
        y *= scalar
        z *= scalar

        return this
    }

    fun mul(other: Vec3f) : Vec3f {
        x *= other.x
        y *= other.y
        z *= other.z

        return this
    }

    fun normalize() : Vec3f{
        var length = length()

        if(length > 0){
            length = 1 / length
        }

        x *= length
        y *= length
        z *= length

        return this
    }

    fun angle(other: Vec3f): Float{
        val mag = this.length() * other.length()
        return if(mag != 0f){
            val cosTheta = dot(other) / mag

            acos(cosTheta.coerceIn(-1f, 1f))
        }else{
            0f
        }
    }

    fun dot(other: Vec3f) : Float{
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    fun add(other: Vec3f) : Vec3f{
        x += other.x
        y += other.y
        z += other.z

        return this
    }

    fun add(x: Float, y: Float, z: Float) : Vec3f{
        this.x += x
        this.y += y
        this.z += z

        return this
    }

    fun sub(other: Vec3f) : Vec3f{
        x -= other.x
        y -= other.y
        z -= other.z

        return this
    }

    fun sub(x: Float, y: Float, z: Float) : Vec3f{
        this.x -= x
        this.y -= y
        this.z -= z

        return this
    }

    fun length() : Float{
        return sqrt(lengthSquared())
    }

    fun lengthSquared() : Float{
        return x.pow(2) + y.pow(2) + z.pow(2)
    }

    fun distance(other: Vec3f) : Float{
        return sqrt(distanceSquared(other))
    }

    fun distanceSquared(other: Vec3f) : Float{
        val x = other.x - x
        val y = other.y - y
        val z = other.z - z

        return x.pow(2) + y.pow(2) + z.pow(2)
    }

}