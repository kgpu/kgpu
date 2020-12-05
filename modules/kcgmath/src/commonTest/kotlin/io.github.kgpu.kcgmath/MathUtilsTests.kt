package io.github.kgpu.kcgmath

import kotlin.test.Test
import kotlin.test.assertEquals

class MathUtilsTests {

    @Test
    fun toRadiansTest() {
        assertEquals(MathUtils.PIf, MathUtils.toRadians(180f))
        assertEquals(MathUtils.PIf / 2, MathUtils.toRadians(90f))
    }

    @Test
    fun toDegreesTest() {
        assertEquals(180f, MathUtils.toDegrees(MathUtils.PIf))
        assertEquals(90f, MathUtils.toDegrees(MathUtils.PIf / 2f))
    }
}
