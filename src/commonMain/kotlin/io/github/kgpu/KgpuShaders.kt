package io.github.kgpu

expect object ShaderCompiler{

    suspend fun compile(name: String, source: String, stage: ShaderType) : ByteArray

}

expect enum class ShaderType{
    VERTEX, FRAGMENT, COMPUTE
}