package io.github.kgpu.kcgmath

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class Vec{

    @Test
    internal fun addTest() {
        val vec1 = Vec3(1f, 2f, 3f)
        val vec2 = Vec3(2f, 1f)

        assertEquals(Vec3(3f, 3f, 3f), vec1.add(vec2))
    }

    @Test
    internal fun addComponentsTest() {
        val vec1 = Vec3(1f, 2f, 3f)

        assertEquals(Vec3(4f, -3f, 10f), vec1.add(3f, -5f, 7f))
    }

    @Test
    internal fun subTest() {
        val vec1 = Vec3(3f, 3f, 3f)
        val vec2 = Vec3(2f, 1f)

        assertEquals(Vec3(1f, 2f, 3f), vec1.sub(vec2))
    }

    @Test
    internal fun subComponentsTest() {
        val vec1 = Vec3(1f, 2f, 3f)

        assertEquals(Vec3(-9f, 12f, -7f), vec1.sub(10f, -10f, 10f))
    }

    @Test
    fun lengthTest() {
        val vec = Vec3(1f, 1f, 1f)

        assertEquals(3f, vec.lengthSquared())
    }

    @Test
    fun distanceTest(){
        val vec1 = Vec3(1f, 1f, 1f)
        val vec2 = Vec3(3f, 3f, 3f)

        assertEquals(sqrt(12f), vec1.distance(vec2))
        assertEquals(sqrt(12f), vec2.distance(vec1))
    }

    @Test
    fun angleTest(){
        val vec1 = Vec3(1f, 0f, 0f)
        val vec2 = Vec3(0f, 1f, 0f)

        assertEquals(MathUtils.PIf / 2f, vec1.angle(vec2))
    }

    @Test
    fun dotTest() {
        val vec1 = Vec3(1f, 2f, 3f)
        val vec2 = Vec3(3f, 2f, 1f);

        assertEquals(10f, vec1.dot(vec2))
    }

    @Test
    fun multiplyScalarTest() {
        val vec1 = Vec3(1f, 2f, 3f)

        assertEquals(Vec3(5f, 10f, 15f), vec1.mul(5f))
    }

    @Test
    fun multiplyTest() {
        val vec1 = Vec3(1f, 2f, 3f)
        val vec2 = Vec3(10f, 10f, 10f)

        assertEquals(Vec3(10f, 20f, 30f), vec1.mul(vec2))
    }

    @Test
    fun normalizeTest(){
        val vec = Vec3(sqrt(18f), -3f, -3f)

        assertEquals(Vec3(sqrt(18f) / 6f, -.5f, -.5f), vec.normalize());
    }

    @Test
    fun normalizeZeroTest(){
        val vec = Vec3()

        assertEquals(Vec3(), vec.normalize());
    }
}