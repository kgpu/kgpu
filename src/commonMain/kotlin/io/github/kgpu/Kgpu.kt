package io.github.kgpu

expect object Kgpu{
    val backendName: String

    fun runLoop(window: Window, func: () -> Unit)
}

expect class Device {

    fun createShaderModule(data: ByteArray) : ShaderModule;

}

expect class ShaderModule{

}

expect class Adapter{

    suspend fun requestDeviceAsync() : Device

}

expect enum class PowerPreference{
    LOW, DEFAULT, PERFORMANCE
}

expect class Window(){

    fun setTitle(title: String)

    fun isCloseRequested() : Boolean

    fun update()

    suspend fun requestAdapterAsync(preference: PowerPreference) : Adapter
}