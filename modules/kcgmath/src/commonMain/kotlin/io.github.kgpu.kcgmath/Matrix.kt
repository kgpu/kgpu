package io.github.kgpu.kcgmath;

import kotlin.math.*;

/**
 * A column major 4x4 matrix backed by floats.
 *
 * ```
 * M00 M01 M02 M03
 * M10 M11 M12 M13
 * M20 M21 M22 M23
 * M30 M31 M32 M33
 * ```
 *
 * To make a matrix from values, use:
 * [Matrix4.fromRows]
 * or
 * [Matrix4.fromCols]
 */
class Matrix4 internal constructor(private val values: FloatArray){

    /**
     * Creates a 4x4 Identity Matrix
     */
    constructor() : this (IDENTITY.copyInto(FloatArray(16)))

    init {
        assertValid(values)
    }

    companion object {
        const val M00 = 0
        const val M10 = 1
        const val M20 = 2
        const val M30 = 3

        const val M01 = 4
        const val M11 = 5
        const val M21 = 6
        const val M31 = 7

        const val M02 = 8
        const val M12 = 9
        const val M22 = 10
        const val M32 = 11

        const val M03 = 12
        const val M13 = 13
        const val M23 = 14
        const val M33 = 15

        /**
        * A 4x4 Identity Matrix
        */
        val IDENTITY = floatArrayOf(
            1f, 0f, 0f, 0f, // column-0
            0f, 1f, 0f, 0f, // column-1
            0f, 0f, 1f, 0f, // column-2
            0f, 0f, 0f, 1f  // column-3
        )


        /**
         * Generates a matrix from row major order. 
         * 
         * 
         * ```kotlin
         * fromCols(
         *   A, B, C, D,
         *   E, F, G, H,
         *   I, J, K, L,
         *   M, N, O, P
         * )
         * ```
         * Produces: 
         * ```
         * A, B, C, D,
         * E, F, G, H,
         * I, J, K, L,
         * M, N, O, P
         * ```
         */
        fun fromRows(
            M00: Float, M01: Float, M02: Float, M03: Float,
            M10: Float, M11: Float, M12: Float, M13: Float,
            M20: Float, M21: Float, M22: Float, M23: Float,
            M30: Float, M31: Float, M32: Float, M33: Float
        ) : Matrix4 {
            return Matrix4(floatArrayOf(
                M00, M10, M20, M30,
                M01, M11, M21, M31,
                M02, M12, M22, M32, 
                M03, M13, M23, M33
            ))
        }

        /**
         * Generates a matrix from column major order. 
         * 
         * 
         * ```kotlin
         * fromCols(
         *   A, B, C, D,
         *   E, F, G, H,
         *   I, J, K, L,
         *   M, N, O, P
         * )
         * ```
         * Produces: 
         * ```
         * A, E, I, M,
         * B, F, J, N
         * C, G, K, O
         * D, H, L, P
         * ```
         */
        fun fromCols(
            M00: Float, M10: Float, M20: Float, M30: Float,
            M01: Float, M11: Float, M21: Float, M31: Float,
            M02: Float, M12: Float, M22: Float, M32: Float,
            M03: Float, M13: Float, M23: Float, M33: Float
        ) : Matrix4 {
            return Matrix4(floatArrayOf(
                M00, M10, M20, M30,
                M01, M11, M21, M31,
                M02, M12, M22, M32, 
                M03, M13, M23, M33
            ))
        }

        private fun assertValid(floats: FloatArray){
            if(floats.size != 16){
                throw UnsupportedOperationException("Invalid matrix! Size = " + floats.size);
            }
        }
    }

    fun add(other: Matrix4) : Matrix4{
        assertValid(other.values)

        for(i in 0..15){
            values[i] += other.values[i]
        }

        return this
    }

    fun sub(other: Matrix4) : Matrix4{
        assertValid(other.values)

        for(i in 0..15){
            values[i] -= other.values[i]
        }

        return this
    }

    fun scale(scalar: Float) : Matrix4 {
        for(i in 0..15){
            values[i] *= scalar
        }

        return this
    }

    fun mul(other: Matrix4) : Matrix4{
        val outputs = FloatArray(16);

        for(row in 0..3){   
            for(column in 0..3){
                val columnStart = column * 4
                var sum = (values[row] * other.values[columnStart] + 
                    values[row + 4] * other.values[columnStart + 1] + 
                    values[row + 8] * other.values[columnStart + 2] + 
                    values[row + 12] * other.values[columnStart + 3])
            
                outputs[row + 4 * column] = sum
            }
        }

        return set(outputs);
    }

    /**
     * Copies the given array into this matrix
     * 
     * @throws UnsupportedOperationException if the passed array does not have a size of 16
     */
    fun set(values: FloatArray) : Matrix4{
        assertValid(values)
        values.copyInto(this.values)

        return this
    }

    fun ortho(left: Float, right: Float, top: Float, bottom: Float, zNear: Float, zFar: Float) : Matrix4{
        set(IDENTITY)

        values[M00] = 2 / (right - left)
        values[M11] = 2 / (top - bottom)
        values[M22] = 1 / (zNear - zFar)
        values[M03] = (right + left) / (left - right)
        values[M13] = (top + bottom) / (bottom - top)
        values[M23] = zNear / (zNear - zFar)
        
        return this
    }

    fun perspective(fov: Float, aspect: Float, zNear: Float, zFar: Float) : Matrix4 {
        values.fill(0f)
        val h = tan(fov * 0.5f)
        values[M00] = 1.0f / (h * aspect)
        values[M11] = 1.0f / h
        values[M22] = zFar / (zNear - zFar)
        values[M23] = zFar * zNear / (zNear - zFar)
        values[M32] = -1f
    
        return this;
    }

    fun lookAt(eye: Vec3, center: Vec3, up: Vec3) : Matrix4{
        set(IDENTITY)

        val direction = eye.copy().sub(center).normalize();
        val left = Vec3(
            up.y * direction.z - up.z * direction.y,
            up.z * direction.x - up.x * direction.z,
            up.x * direction.y - up.y * direction.x
        ).normalize()

        val newUp = Vec3(
            direction.y * left.z - direction.z * left.y,
            direction.z * left.x - direction.x * left.z,
            direction.x * left.y - direction.y * left.x,
        )

        values[M00] = left.x
        values[M10] = newUp.x
        values[M20] = direction.x
        values[M30] = 0f
        values[M01] = left.y
        values[M11] = newUp.y 
        values[M21] = direction.y
        values[M31] = 0f
        values[M02] = left.z
        values[M12] = newUp.z
        values[M22] = direction.z 
        values[M32] = 0f
        values[M03] = -left.dot(eye)
        values[M13] = -newUp.dot(eye)
        values[M23] = -direction.dot(eye)
        values[M33] = 1f

        return this
    }

    fun rotate(angleX: Float, angleY: Float, angleZ: Float) : Matrix4{
        val sinX = sin(angleX)
        val sinY = sin(angleY)
        val sinZ = sin(angleZ)
        val cosX = cos(angleX)
        val cosY = cos(angleY)
        val cosZ = cos(angleZ)

        val nm01 = values[M01] * cosX + values[M02] * sinX
        val nm11 = values[M11] * cosX + values[M12] * sinX 
        val nm21 = values[M21] * cosX + values[M22] * sinX 
        val nm31 = values[M31] * cosX + values[M32] * sinX 
        val nm02 = values[M01] * -sinX + values[M02] * cosX
        val nm12 = values[M11] * -sinX + values[M12] * cosX
        val nm22 = values[M21] * -sinX + values[M22] * cosX
        val nm32 = values[M31] * -sinX + values[M32] * cosX
        val nm00 = values[M00] * cosY + nm02 * -sinY 
        val nm10 = values[M10] * cosY + nm12 * -sinY 
        val nm20 = values[M20] * cosY + nm22 * -sinY 
        val nm30 = values[M30] * cosY + nm32 * -sinY 

        values[M02] = values[M00] * sinY + nm02 * cosY
        values[M12] = values[M10] * sinY + nm12 * cosY
        values[M22] = values[M20] * sinY + nm22 * cosY
        values[M32] = values[M30] * sinY + nm32 * cosY

        values[M00] = nm00 * cosZ + nm01 * sinZ
        values[M10] = nm10 * cosZ + nm11 * sinZ
        values[M20] = nm20 * cosZ + nm21 * sinZ
        values[M30] = nm30 * cosZ + nm31 * sinZ
        
        values[M01] = nm00 * -sinZ + nm01 * cosZ
        values[M11] = nm10 * -sinZ + nm11 * cosZ
        values[M21] = nm20 * -sinZ + nm21 * cosZ
        values[M31] = nm30 * -sinZ + nm31 * cosZ

        return this 
    }

    fun transpose() : Matrix4{
        fun swap(indexA: Int, indexB: Int){
            val temp = values[indexA]
            values[indexA] = values[indexB]
            values[indexB] = temp
        }

        swap(M10, M01)
        swap(M20, M02)
        swap(M30, M03)
        swap(M21, M12)
        swap(M31, M13)
        swap(M32, M23)

        return this;
    }

    fun invert() : Matrix4{
        val a = values[M00] * values[M11] - values[M10] * values[M01]
        val b = values[M00] * values[M21] - values[M20] * values[M01]
        val c = values[M00] * values[M31] - values[M30] * values[M01]
        val d = values[M10] * values[M21] - values[M20] * values[M11]
        val e = values[M10] * values[M31] - values[M30] * values[M11]
        val f = values[M20] * values[M31] - values[M30] * values[M21]
        val g = values[M02] * values[M13] - values[M12] * values[M03]
        val h = values[M02] * values[M23] - values[M22] * values[M03]
        val i = values[M02] * values[M33] - values[M32] * values[M03]
        val j = values[M12] * values[M23] - values[M22] * values[M13]
        val k = values[M12] * values[M33] - values[M32] * values[M13]
        val l = values[M22] * values[M33] - values[M32] * values[M23]
        val det = 1 / (a * l - b * k + c * j + d * i - e * h + f * g)

        val nm00 = multAdd(values[M11], l, multAdd(-values[M21], k, values[M31] * j)) * det
        val nm10 = multAdd(-values[M10], l, multAdd(values[M20], k, -values[M30] * j)) * det
        val nm20 = multAdd(values[M13], f, multAdd(-values[M23], e, values[M33] * d)) * det
        val nm30 = multAdd(-values[M12], f, multAdd(values[M22], e, -values[M32] * d)) * det
        val nm01 = multAdd(-values[M01], l, multAdd(values[M21], i, -values[M31] * h)) * det
        val nm11 = multAdd( values[M00], l, multAdd(-values[M20], i,  values[M30] * h)) * det;
        val nm21 = multAdd(-values[M03], f, multAdd( values[M23], c, -values[M33] * b)) * det;
        val nm31 = multAdd( values[M02], f, multAdd(-values[M22], c,  values[M32] * b)) * det;
        val nm02 = multAdd( values[M01], k, multAdd(-values[M11], i,  values[M31] * g)) * det;
        val nm12 = multAdd(-values[M00], k, multAdd( values[M10], i, -values[M30] * g)) * det;
        val nm22 = multAdd( values[M03], e, multAdd(-values[M13], c,  values[M33] * a)) * det;
        val nm32 = multAdd(-values[M02], e, multAdd( values[M12], c, -values[M32] * a)) * det;
        val nm03 = multAdd(-values[M01], j, multAdd( values[M11], h, -values[M21] * g)) * det;
        val nm13 = multAdd( values[M00], j, multAdd(-values[M10], h,  values[M20] * g)) * det;
        val nm23 = multAdd(-values[M03], d, multAdd( values[M13], b, -values[M23] * a)) * det;
        val nm33 = multAdd( values[M02], d, multAdd(-values[M12], b,  values[M22] * a)) * det;
        
        values[M00] = nm00
        values[M10] = nm10
        values[M20] = nm20
        values[M30] = nm30
        values[M01] = nm01
        values[M11] = nm11
        values[M21] = nm21
        values[M31] = nm31
        values[M02] = nm02
        values[M12] = nm12
        values[M22] = nm22
        values[M32] = nm32
        values[M03] = nm03
        values[M13] = nm13
        values[M23] = nm23
        values[M33] = nm33 

        return this
    }

    private fun multAdd(a: Float, b: Float, c: Float) : Float {
        return a * b + c
    }

    /**
     * Creates a copy of this matrix. Changing the copy will not affect
     * this array.
     */
    fun clone() : Matrix4{
        return Matrix4(values.copyOf())
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other !is Matrix4) return false

        values.forEachIndexed{ index, value -> 
            if(other.values[index] != value) return false
        }

        return true
    }

    override fun hashCode(): Int{
        return values.hashCode()
    }

    override fun toString() : String {
        val builder = StringBuilder()
        builder.append("{")

        for(i in 0..15){
            builder.append(values[i])

            if(i != 15) builder.append(", ")
        }

        builder.append("}")
        return builder.toString()
    }

    /**
     * Returns the array backing this matrix in column major order. Warning: changing the returned 
     * array will change this matrix.
     */
    fun toFloats() : FloatArray{
        return values
    }

    /**
     * Returns a byte array of the values stored in this matrix. 
     * The values will be in column major order
     */
    fun toBytes() : ByteArray {
        val bytes = ByteArray(64)

        values.forEachIndexed { index, float ->
            run {
                val i = index * 4
                val bits = float.toRawBits()

                bytes[i + 3] = (bits shr 24).toByte()
                bytes[i + 2] = (bits shr 16).toByte()
                bytes[i + 1] = (bits shr 8).toByte()
                bytes[i + 0] = bits.toByte()
            }
        }

        return bytes
    }
}