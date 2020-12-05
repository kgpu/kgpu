package io.github.kgpu.kshader

import kotlin.js.Promise
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.Uint32Array

private const val glslangGlobalVariable = "window.kshader_glslang"

private const val glslangUrl =
    "https://cdn.jsdelivr.net/npm/@webgpu/glslang@0.0.15/dist/web-devel/glslang.js"

private const val scriptSrc =
    """
    import * as glslang from '$glslangUrl'

    $glslangGlobalVariable = glslang.default()
    console.log("KShader loaded glslang: ", $glslangGlobalVariable)
"""

internal object GlslangLibrary {

    fun init() {
        val script = document.createElement("script")
        script.id = "kshader_internal_glslang"
        script.setAttribute("type", "module")
        script.innerHTML = scriptSrc

        document.body?.append(script)
    }

    suspend fun getGlslang(): Glslang {
        if (js(glslangGlobalVariable) == undefined)
            throw RuntimeException("Failed to call KShader.init()!")

        return (js(glslangGlobalVariable) as Promise<Glslang>).await()
    }
}

internal external class Glslang {

    fun compileGLSL(glsl: String, shaderType: String, debug: Boolean): Uint32Array
}
