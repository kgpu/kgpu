package io.github.kgpu

expect object KgpuFiles{

    suspend fun loadInternal(path: String) : ByteArray

}