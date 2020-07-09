package io.github.kgpu

import java.io.IOException
import java.io.InputStream


actual object KgpuFiles {

    actual suspend fun loadInternal(path: String) : ByteArray{
        val inputStream: InputStream = KgpuFiles::class.java.getResourceAsStream(path)
            ?: throw RuntimeException("Failed to find file: $path")

        return try {
            inputStream.readAllBytes()
        } catch (e: IOException) {
            throw RuntimeException("Failed to read file $path", e)
        }
    }

}