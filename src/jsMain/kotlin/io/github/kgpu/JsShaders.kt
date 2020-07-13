package io.github.kgpu

import io.github.kgpu.internal.ArrayBufferUtils
import io.github.kgpu.internal.GlslangLibrary

actual object ShaderCompiler {

    actual suspend fun compile(name: String, source: String, stage: ShaderType): ByteArray {
        val glslang = GlslangLibrary.getGlslang()
        val data = glslang.compileGLSL(source, stage.jsType, false)

        return ArrayBufferUtils.toByteArray(data.buffer)
    }

}

actual enum class ShaderType(val jsType: String){
    VERTEX("vertex"), FRAGMENT("fragment"), COMPUTE("compute")
}