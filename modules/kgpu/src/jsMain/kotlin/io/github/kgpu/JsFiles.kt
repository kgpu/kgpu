package io.github.kgpu

import io.github.kgpu.internal.ArrayBufferUtils
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.get
import org.w3c.fetch.RequestInit

actual object KgpuFiles {

    actual suspend fun loadInternal(path: String): ByteArray {
        val response = window.fetch(path, RequestInit()).await().arrayBuffer().await()

        return ArrayBufferUtils.toByteArray(response)
    }

    actual suspend fun loadInternalUtf8(path: String): String {
        val response = window.fetch(path, RequestInit()).await().text()

        return response.await()
    }
}
