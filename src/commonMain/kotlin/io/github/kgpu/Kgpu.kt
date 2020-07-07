package io.github.kgpu

expect object WebGPU{
    val backendName: String
}

object Kgpu {

    fun init(){
        println("Initializing webgpu: ${WebGPU.backendName}")
    }
}