package io.github.kgpu.kcgmath

import kotlin.test.*

class Matrix4Tests {

    @Test
    fun non16SizedArrayThrowsTest() {
        assertFailsWith<UnsupportedOperationException> { Matrix4(floatArrayOf(1f, 2f, 3f, 4f)) }
    }

    @Test
    fun notEqualTest() {
        val matrix1 =
            Matrix4(
                floatArrayOf(
                    1f,
                    2f,
                    3f,
                    4f,
                    1f,
                    2f,
                    3f,
                    4f,
                    1f,
                    2f,
                    3f,
                    4f,
                    1f,
                    2f,
                    3f,
                    4f,
                ))

        val matrix2 =
            Matrix4(
                floatArrayOf(
                    4f,
                    3f,
                    2f,
                    1f,
                    4f,
                    3f,
                    2f,
                    1f,
                    4f,
                    3f,
                    2f,
                    1f,
                    4f,
                    3f,
                    2f,
                    1f,
                ))

        assertNotEquals(matrix1, matrix2)
    }

    @Test
    fun addTest() {
        val matrix1 =
            Matrix4(
                floatArrayOf(
                    1f,
                    2f,
                    3f,
                    4f,
                    1f,
                    2f,
                    3f,
                    4f,
                    1f,
                    2f,
                    3f,
                    4f,
                    1f,
                    2f,
                    3f,
                    4f,
                ))

        val matrix2 =
            Matrix4(
                floatArrayOf(
                    4f,
                    3f,
                    2f,
                    1f,
                    4f,
                    3f,
                    2f,
                    1f,
                    4f,
                    3f,
                    2f,
                    1f,
                    4f,
                    3f,
                    2f,
                    1f,
                ))

        val expected =
            Matrix4(
                floatArrayOf(
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                    5f,
                ))

        assertEquals(expected, matrix1.add(matrix2))
    }

    @Test
    fun subTest() {
        val matrix1 =
            Matrix4(
                floatArrayOf(15f, 14f, 13f, 12f, 11f, 10f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f, 0f))

        val matrix2 =
            Matrix4(
                floatArrayOf(
                    1f,
                    2f,
                    2f,
                    1f,
                    1f,
                    2f,
                    2f,
                    1f,
                    1f,
                    2f,
                    2f,
                    1f,
                    1f,
                    2f,
                    2f,
                    1f,
                ))

        val expected =
            Matrix4(
                floatArrayOf(14f, 12f, 11f, 11f, 10f, 8f, 7f, 7f, 6f, 4f, 3f, 3f, 2f, 0f, -1f, -1f))

        assertEquals(expected, matrix1.sub(matrix2))
    }

    @Test
    fun scaleTest() {
        val matrix =
            Matrix4(floatArrayOf(1f, 2f, 3f, 4f, 4f, 3f, 2f, 1f, 1f, 2f, 3f, 4f, 4f, 3f, 2f, 1f))

        val expected =
            Matrix4(floatArrayOf(2f, 4f, 6f, 8f, 8f, 6f, 4f, 2f, 2f, 4f, 6f, 8f, 8f, 6f, 4f, 2f))

        assertEquals(expected, matrix.scale(2f))
    }

    @Test
    fun cloneTest() {
        val matrix = Matrix4()
        val clone = matrix.clone()

        assertEquals(clone, matrix)
        assertFalse(matrix === clone)
    }

    @Test
    fun cloneChangeValuesTest() {
        val matrix = Matrix4()
        val clone = matrix.clone()
        clone.scale(3f)

        assertNotEquals(clone, matrix)
    }

    @Test
    fun multiplyTest() {
        val matrix1 =
            Matrix4(
                floatArrayOf(
                    1f,
                    2f,
                    -1f,
                    -2f,
                    3f,
                    4f,
                    -3f,
                    -4f,
                    5f,
                    6f,
                    -5f,
                    -6f,
                    7f,
                    8f,
                    -7f,
                    -8f,
                ))

        val matrix2 =
            Matrix4(
                floatArrayOf(
                    1f, 5f, -8f, -4f, 2f, 6f, -7f, -3f, 3f, 7f, -6f, -2f, 4f, 8f, -5f, -1f))

        val expected =
            Matrix4(
                floatArrayOf(
                    -52f,
                    -58f,
                    52f,
                    58f,
                    -36f,
                    -38f,
                    36f,
                    38f,
                    -20f,
                    -18f,
                    20f,
                    18f,
                    -4f,
                    2f,
                    4f,
                    -2f))

        assertEquals(expected, matrix1.mul(matrix2))
    }

    @Test
    fun toFloatsTest() {
        val floats = FloatArray(16)
        val matrix = Matrix4(floats)

        assertSame(floats, matrix.toFloats())
    }

    @Test
    fun transposeTest() {
        val matrix =
            Matrix4(
                floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f))

        val expected =
            Matrix4(
                floatArrayOf(1f, 5f, 9f, 13f, 2f, 6f, 10f, 14f, 3f, 7f, 11f, 15f, 4f, 8f, 12f, 16f))

        assertEquals(expected, matrix.transpose())
    }

    @Test
    fun invertIdentityTest() {
        val matrix = Matrix4().invert()

        val expected = Matrix4()

        assertEquals(expected, matrix)
    }

    @Test
    fun invertTest() {
        val matrix =
            Matrix4(floatArrayOf(1f, 3f, 4f, 0f, 4f, 5f, 1f, 1f, 5f, 1f, 7f, 2f, 7f, 6f, 9f, 2f))
                .invert()

        val expected =
            Matrix4(
                    floatArrayOf(
                        -43f,
                        -28f,
                        -37f,
                        51f,
                        16f,
                        12f,
                        11f,
                        -17f,
                        3f,
                        -2f,
                        1f,
                        0f,
                        89f,
                        71f,
                        92f,
                        -119f))
                .scale(1f / 17f)

        assertEquals(expected, matrix)
    }

    @Test
    fun fromColsTest() {
        val matrix =
            Matrix4.fromCols(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)

        val expected =
            Matrix4(
                floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f))

        assertEquals(expected, matrix)
    }

    @Test
    fun fromRowsTest() {
        val matrix =
            Matrix4.fromRows(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)

        val expected =
            Matrix4(
                floatArrayOf(1f, 5f, 9f, 13f, 2f, 6f, 10f, 14f, 3f, 7f, 11f, 15f, 4f, 8f, 12f, 16f))

        assertEquals(expected, matrix)
    }
}
