@file:JsModule("gl-matrix")
@file:JsNonModule()
package io.github.kgpu.internal

external object glMatrix{
    val EPSILON: Float
}

external object mat4{

    fun create() : Mat4

    fun multiplyScalar(out: Mat4, a: Mat4, scalar: Float)

    fun translate(out: Mat4, a: Mat4, vec: Vec3)

}

external object vec3{
    fun create() : Vec3

    fun fromValues(x: Float, y: Float, z: Float) : Vec3
}

external class Mat4

external class Vec3