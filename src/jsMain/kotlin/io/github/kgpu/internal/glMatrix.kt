@file:JsModule("gl-matrix")
@file:JsNonModule()

package io.github.kgpu.internal

import io.github.kgpu.Matrix4f
import io.github.kgpu.Vec3f

external object glMatrix {
    val EPSILON: Float
}

external object mat4 {

    fun create(): Mat4

    fun clone(a: Mat4): Mat4

    fun multiplyScalar(out: Mat4, a: Mat4, scalar: Float)

    fun translate(out: Mat4, a: Mat4, vec: Vec3)

    fun ortho(out: Mat4, left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float)

    fun lookAt(out: Mat4, eye: Vec3, center: Vec3, up: Vec3)

    fun mul(out: Mat4, a: Mat4, b: Mat4)

    fun perspective(out: Mat4, fov: Float, aspect: Float, near: Float, far: Float)

    fun rotateX(out: Mat4, a: Mat4, rad: Float)

    fun rotateY(out: Mat4, a: Mat4, rad: Float)

    fun rotateZ(out: Mat4, a: Mat4, rad: Float)

    fun transpose(out: Mat4, a: Mat4)

    fun invert(out: Mat4, a: Mat4)

}

external object vec3 {
    fun fromValues(x: Float, y: Float, z: Float): Vec3
}

external class Mat4

external class Vec3