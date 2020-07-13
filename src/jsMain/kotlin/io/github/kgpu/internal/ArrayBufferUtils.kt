package io.github.kgpu.internal

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get


object ArrayBufferUtils {

    fun toByteArray(buffer: ArrayBuffer) : ByteArray{
        val bytes = Uint8Array(buffer)
        val output = ByteArray(bytes.length);

        for(i : Int in 0..bytes.length){
            output[i] = bytes[i];
        }

        return output
    }

}