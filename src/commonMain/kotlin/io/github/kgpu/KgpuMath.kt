package io.github.kgpu

import kotlin.math.PI

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

expect class Vec3f(x: Float, y: Float, z: Float){
    var x : Float
    var y : Float
    var z : Float

    constructor()

    fun mul(scalar: Float) : Vec3f

    fun normalize() : Vec3f

}