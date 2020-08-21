package io.github.kgpu.kcgmath;


object MathUtils {
    const val PIf = kotlin.math.PI.toFloat()

    fun toRadians(deg: Float) : Float{
        return deg / 180f * PIf
    }
}