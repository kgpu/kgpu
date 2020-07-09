package io.github.kgpu

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
        val bytes = Uint8Array(response)
        val output = ByteArray(bytes.length);

        for(i : Int in 0..bytes.length){
            output[i] = bytes[i];
        }

        return output;
    }

}