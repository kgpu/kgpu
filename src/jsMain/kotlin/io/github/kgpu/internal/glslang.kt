package io.github.kgpu.internal

import io.github.kgpu.ShaderType
import kotlinx.coroutines.await
import org.khronos.webgl.Uint32Array
import kotlin.browser.document
import kotlin.js.Promise

private const val glslangGlobalVariable = "window.test"
private const val glslangUrl = "https://cdn.jsdelivr.net/npm/@webgpu/glslang@0.0.15/dist/web-devel/glslang.js"
private const val scriptSrc = """
    import * as glslang from '$glslangUrl'

    $glslangGlobalVariable = glslang.default()
    console.log("Setup glslang: ", $glslangGlobalVariable)
"""

internal object GlslangLibrary{

    fun init(){
        val script = document.createElement("script")
        script.id = "kgpu_internal_glslang"
        script.setAttribute("type", "module")
        script.innerHTML = scriptSrc

        document.body?.append(script)
    }

    suspend fun getGlslang() : Glslang{
        return (js(glslangGlobalVariable) as Promise<Glslang>).await()
    }
}

internal external class Glslang{

    fun compileGLSL(glsl: String, shaderType: String, debug: Boolean) : Uint32Array

}