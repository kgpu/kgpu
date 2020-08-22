package io.github.kgpu

import io.github.kgpu.internal.ArrayBufferUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import org.w3c.fetch.SAME_ORIGIN
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.window

actual object KgpuFiles {

    actual suspend fun loadInternal(path: String) : ByteArray {
        val response = window.fetch(path, RequestInit()).await().arrayBuffer().await()

        return ArrayBufferUtils.toByteArray(response);
    }

    actual suspend fun loadInternalUtf8(path: String) : String {
        val response = window.fetch(path, RequestInit()).await().text()

        return response.await();
    }

}