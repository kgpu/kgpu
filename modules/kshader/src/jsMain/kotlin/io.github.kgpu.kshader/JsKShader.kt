package io.github.kgpu.kshader

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

actual object KShader {

    actual fun init(){
        GlslangLibrary.init()
    }

    actual suspend fun compile(name: String, source: String, type: KShaderType): ByteArray {
        val glslang = GlslangLibrary.getGlslang()
        val data = glslang.compileGLSL(source, type.jsType, false)

        return toByteArray(data.buffer)
    }

}

actual enum class KShaderType(internal val jsType: String){
    VERTEX("vertex"), FRAGMENT("fragment"), COMPUTE("compute")
}

fun toByteArray(buffer: ArrayBuffer) : ByteArray{
    val bytes = Uint8Array(buffer)
    val output = ByteArray(bytes.length);

    for(i : Int in 0..bytes.length){
        output[i] = bytes[i];
    }

    return output
}