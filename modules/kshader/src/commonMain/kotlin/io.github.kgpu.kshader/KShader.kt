package io.github.kgpu.kshader

expect object KShader {
    fun init()

    /**
     * Compiles a GLSL shader into SPIR-V
     *
     * Desktop Compiler:
     * [shaderc](https://github.com/LWJGL/lwjgl3/tree/master/modules/lwjgl/shaderc)
     *
     * Web Compiler: [@webgpu/glslang](https://www.npmjs.com/package/@webgpu/glslang)
     */
    suspend fun compile(name: String, source: String, type: KShaderType): ByteArray
}

expect enum class KShaderType {
    VERTEX,
    FRAGMENT,
    COMPUTE
}
