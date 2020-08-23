package io.github.kgpu.kcgmath

import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.joml.Matrix4f as JomlMatrix
import java.nio.ByteBuffer;

typealias KgpuMatrix = Matrix4

class JOMLCompareTests {
    
    @Test
    fun identityMatrixTest() {
        val joml = JomlMatrix()
        val kgpu = KgpuMatrix()

        assertEqual(joml, kgpu)
    }

    @Test
    fun assertEqualCustomTest() {
        val joml = JomlMatrix()
        val kgpu = KgpuMatrix().scale(5f)

        
        assertFailsWith<RuntimeException> {
            assertEqual(joml, kgpu)
        }
    }

    @Test
    fun multiplyTest(){
        val floats = floatArrayOf(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f, 
            13f, 14f, 15f, 16f
        )

        val joml = JomlMatrix().mul(JomlMatrix().set(floats))
        val kgpu = KgpuMatrix().mul(KgpuMatrix(floats))

        assertEqual(joml, kgpu)
    } 

    @Test
    fun orthoTest(){
        val joml = JomlMatrix().ortho(10f, 20f, 30f, 40f, 50f, 60f, true)
        val kgpu = KgpuMatrix().ortho(10f, 20f, 40f, 30f, 50f, 60f)

        assertEqual(joml, kgpu)
    }

    @Test
    fun perspectiveTest(){
        val joml = JomlMatrix().perspective(1.5f, 1f, -25f, 25f, true)
        val kgpu = KgpuMatrix().perspective(1.5f, 1f, -25f, 25f)

        assertEqual(joml, kgpu)
    }

    @Test
    fun lookAtTest(){
        val joml = JomlMatrix().lookAt(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f
        )
        val kgpu = KgpuMatrix().lookAt(
            Vec3(1f, 2f, 3f),
            Vec3(4f, 5f, 6f),
            Vec3(7f, 8f, 9f),
        )

        assertEqual(joml, kgpu)
    }

    @Test
    fun rotateTest(){
        val joml = JomlMatrix().assume(0).rotateXYZ(-1f, .5f, 1f)
        val kgpu = KgpuMatrix().rotate(-1f, .5f, 1f)

        assertEqual(joml, kgpu)
    }

    @Test
    fun transpose() {
        val floats = floatArrayOf(
            1f, 3f, 5f, 7f,
            9f, 11f, 13f, 15f,
            17f, 19f, 21f, 23f,
            25f, 27f, 29f, 31f
        )
        val joml = JomlMatrix().set(floats).transpose()
        val kgpu = KgpuMatrix(floats).transpose()

        assertEqual(joml, kgpu)
    }

    @Test
    fun invert() {
        val floats = floatArrayOf(
            4f, 32f, -12f, 0f,
            6f, 76f, 91f, 55f,
            18f, 33f, -43f, 44f,
            2f, 13f, 47f, -9f  
        )
        val joml = JomlMatrix().set(floats).invert()
        val kgpu = KgpuMatrix(floats).invert()

        assertEqual(joml, kgpu)
    }

    private fun assertEqual(joml: JomlMatrix, kgpu: KgpuMatrix){
        val jomlFloats = joml.get(FloatArray(16))
        val kgpuFloats = kgpu.toFloats()
        val epsilon = .000001
        for(i in 0..15){
            if(kotlin.math.abs(jomlFloats[i] - kgpuFloats[i]) > epsilon){
                throw RuntimeException("Matrices do not match at index $i. \n\tJOML = ${jomlFloats[i]}   KGPU = ${kgpuFloats[i]}. \n" 
                    + "\tJOML = ${jomlFloats.joinToString()}  \n\tKGPU = ${kgpuFloats.joinToString()}")
            }
        }
    }

}