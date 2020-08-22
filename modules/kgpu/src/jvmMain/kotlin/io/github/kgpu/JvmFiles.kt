package io.github.kgpu

import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets


actual object KgpuFiles {

    actual suspend fun loadInternal(path: String): ByteArray {
        val jarPath = toJvmJarPath(path)

        val inputStream: InputStream = KgpuFiles::class.java.getResourceAsStream(jarPath)
            ?: throw RuntimeException("Failed to find file: $path")

        return try {
            inputStream.readAllBytes()
        } catch (e: IOException) {
            throw RuntimeException("Failed to read file $path", e)
        }
    }

    fun toJvmJarPath(path: String): String {
        return if (!path.startsWith("/")) {
            "/$path"
        } else {
            path
        }
    }

    actual suspend fun loadInternalUtf8(path: String): String {
        return String(loadInternal(path), StandardCharsets.UTF_8)
    }

}