package io.github.kgpu

/**
 * Compiles a GLSL shader into SPIR-V
 *
 * Desktop Compiler: [shaderc](https://github.com/LWJGL/lwjgl3/tree/master/modules/lwjgl/shaderc)
 *
 * Web Compiler: [@webgpu/glslang](https://www.npmjs.com/package/@webgpu/glslang)
 */
expect object ShaderCompiler{

    suspend fun compile(name: String, source: String, stage: ShaderType) : ByteArray

}

expect enum class ShaderType{
    VERTEX, FRAGMENT, COMPUTE
}