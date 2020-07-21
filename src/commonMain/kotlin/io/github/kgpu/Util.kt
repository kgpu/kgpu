package io.github.kgpu

object ShaderUtils {

    suspend fun fromInternalTextFile(device: Device, file: String, type: ShaderType): ShaderModule {
        val shaderSrc = KgpuFiles.loadInternalUtf8(file)

        return fromSource(device, file, shaderSrc, type)
    }

    suspend fun fromSource(device: Device, name: String, src: String, type: ShaderType): ShaderModule {
        val vertexShader = ShaderCompiler.compile(name, src, type)

        return device.createShaderModule(vertexShader)
    }

}

object BufferUtils {

    fun createBufferFromData(device: Device, label: String, data: ByteArray, usage: Long): Buffer {
        return device.createBufferWithData(
            BufferDescriptor(
                label,
                data.size.toLong(),
                usage,
                true
            ),
            data
        )
    }

    fun createFloatBuffer(device: Device, label: String, data: FloatArray, usage: Long): Buffer {
        return createBufferFromData(device, label, ByteUtils.toByteArray(data), usage)
    }

    fun createShortBuffer(device: Device, label: String, data: ShortArray, usage: Long): Buffer {
        return createBufferFromData(device, label, ByteUtils.toByteArray(data), usage)
    }

    fun createIntBuffer(device: Device, label: String, data: IntArray, usage: Long): Buffer {
        return createBufferFromData(device, label, ByteUtils.toByteArray(data), usage)
    }
}

object ByteUtils {
    fun toByteArray(floatArray: FloatArray): ByteArray {
        val bytes = ByteArray(floatArray.size * 4)
        floatArray.forEachIndexed { index, float ->
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

    fun toFloatArray(byteArray: ByteArray): FloatArray {
        val floats = FloatArray(byteArray.size / 4)

        for (i in floats.indices) {
            val byteI = i * 4
            val bits = (byteArray[byteI].toInt() and 0xFF) or
                    ((byteArray[byteI + 1].toInt() and 0xFF) shl 8) or
                    ((byteArray[byteI + 2].toInt() and 0xFF) shl 16) or
                    ((byteArray[byteI + 3].toInt() and 0xFF) shl 24)

            floats[i] = Float.fromBits(bits)
        }

        return floats
    }

    fun toIntArray(byteArray: ByteArray): IntArray {
        val ints = IntArray(byteArray.size / 4)

        for (i in ints.indices) {
            val byteI = i * 4
            ints[i] = byteArray[byteI].toInt() or
                    (byteArray[byteI + 1].toInt() shl 8) or
                    (byteArray[byteI + 2].toInt() shl 16) or
                    (byteArray[byteI + 3].toInt() shl 24)
        }

        return ints
    }

    fun toByteArray(intArray: IntArray): ByteArray {
        val bytes = ByteArray(intArray.size * 4)
        intArray.forEachIndexed { index, bits ->
            run {
                val i = index * 4

                bytes[i + 3] = (bits shr 24).toByte()
                bytes[i + 2] = (bits shr 16).toByte()
                bytes[i + 1] = (bits shr 8).toByte()
                bytes[i + 0] = bits.toByte()
            }
        }

        return bytes
    }

    fun toByteArray(shortArray: ShortArray): ByteArray {
        val bytes = ByteArray(shortArray.size * 2)
        shortArray.forEachIndexed { index, value ->
            run {
                val i = index * 2

                bytes[i + 1] = (value.toInt() shr 8).toByte()
                bytes[i + 0] = value.toByte()
            }
        }

        return bytes
    }
}