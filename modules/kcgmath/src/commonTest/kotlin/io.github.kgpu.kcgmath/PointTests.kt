package io.github.kgpu.kcgmath

import kotlin.test.assertEquals

class PointTests {

    fun toPoint3Test() {
        val pt = Point2(3, 2).toPoint3(1)
        val expected = Point3(3, 2, 1)

        assertEquals(expected, pt)
    }

    fun toPoint2Test() {
        val pt = Point3(10, 8, 5).toPoint2()
        val expected = Point2(10, 8)

        assertEquals(expected, pt)
    }
}
