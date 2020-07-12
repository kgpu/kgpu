@file:JsModule("gl-matrix")
@file:JsNonModule()
package io.github.kgpu.internal

external object glMatrix{
    val EPSILON: Float
}

external object mat4{

    fun create() : Mat4

}

external class Mat4