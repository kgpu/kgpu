package io.github.kgpu

object ShaderUtils{

    suspend fun fromInternalTextFile(device: Device, file: String, type: ShaderType) : ShaderModule{
        val shaderSrc = KgpuFiles.loadInternalUtf8(file)
        val vertexShader = ShaderCompiler.compile(file, shaderSrc, type)

        return device.createShaderModule(vertexShader)
    }

}

object BufferUtils{

    fun createBufferFromData(device: Device, data: ByteArray, usage: Long) : Buffer{
        return device.createBufferWithData(
            BufferDescriptor(
                data.size.toLong(),
                usage,
                true
            ),
            data
        )
    }

    fun createFloatBuffer(device: Device, data: FloatArray, usage: Long) : Buffer{
        return createBufferFromData(device, ByteUtils.toByteArray(data), usage)
    }

    fun createShortBuffer(device: Device, data: ShortArray, usage: Long) : Buffer{
        return createBufferFromData(device, ByteUtils.toByteArray(data), usage)
    }
}

object ByteUtils{
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