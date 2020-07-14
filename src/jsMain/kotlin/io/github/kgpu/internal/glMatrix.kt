@file:JsModule("gl-matrix")
@file:JsNonModule()
package io.github.kgpu.internal

import io.github.kgpu.Matrix4f

external object glMatrix{
    val EPSILON: Float
}

external object mat4{

    fun create() : Mat4

    fun multiplyScalar(out: Mat4, a: Mat4, scalar: Float)

    fun translate(out: Mat4, a: Mat4, vec: Vec3)

    fun ortho(out: Mat4, left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float)

    fun lookAt(out: Mat4, eye: Vec3, center: Vec3, up: Vec3)

    fun mul(out: Mat4, a: Mat4, b: Mat4)

    fun perspective(out: Mat4, fov: Float, aspect: Float, near: Float, far: Float)
}

external object vec3{
    fun create() : Vec3

    fun fromValues(x: Float, y: Float, z: Float) : Vec3
}

external class Mat4

external class Vec3

/**
 * Generates a look-at matrix with the given eye position, focal point, and up axis.
 * If you want a matrix that actually makes an object look at another object, you should use targetTo instead.
 *
 * @param {mat4} out mat4 frustum matrix will be written into
 * @param {ReadonlyVec3} eye Position of the viewer
 * @param {ReadonlyVec3} center Point the viewer is looking at
 * @param {ReadonlyVec3} up vec3 pointing up
 * @returns {mat4} out
 */
