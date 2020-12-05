package io.github.kgpu.kcgmath

/** Miscellaneous utility functions */
object MathUtils {
    /** A Float Version of PI to prevent unneccessary casts */
    const val PIf = kotlin.math.PI.toFloat()

    /** Converts degreees to radians */
    fun toRadians(deg: Float): Float {
        return deg / 180f * PIf
    }

    /** Converts radians to degrees */
    fun toDegrees(rad: Float): Float {
        return rad / PIf * 180f
    }
}
