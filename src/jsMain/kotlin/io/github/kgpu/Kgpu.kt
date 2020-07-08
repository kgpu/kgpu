package io.github.kgpu

import kotlinx.coroutines.*
import kotlin.js.Promise
import kotlin.browser.window as jsWindow
import kotlin.browser.document as jsDocument

actual object Kgpu {
    actual val backendName: String = "Web"

    actual fun runLoop(window: Window, func: () -> Unit) {
        func();

        jsWindow.requestAnimationFrame {
            runLoop(window, func)
        };
    }

}

actual class Window actual constructor() {

    actual fun setTitle(title: String) {
        jsDocument.title = title
    }

    actual fun isCloseRequested(): Boolean {
        return false
    }

    actual fun update() {

    }

    actual suspend fun requestAdapterAsync(preference: PowerPreference): Adapter{
        return Adapter((js("navigator.gpu.requestAdapter()") as Promise<GPUAdapter>).await())
    }
}

open external class GPUObjectBase{
    val label: String
}

open external class GPUObjectDescriptorBase{
    val label: String
}


actual class Adapter(val jsType: GPUAdapter){

    actual suspend fun requestDeviceAsync() : Device {
        return Device(jsType.requestDevice().await())
    }

    override fun toString(): String {
        return "Adapter($jsType)"
    }

}

open external class GPUAdapter {
    val name : String
    val extensions : List<GPUExtensionName>

    fun requestDevice(): Promise<GPUDevice>
}

/**
 * Eventually will be external once implemented in browsers
 */
enum class GPUExtensionName {
    TextureCompressionBC,
    PipelineStatisticsQuery,
    TimestampQuery,
    DepthClamping
}

actual enum class PowerPreference(jsType: GPUPowerPreference?) {
    LOW(GPUPowerPreference.LOW_POWER),
    DEFAULT(null),
    PERFORMANCE(GPUPowerPreference.HIGH_PERFORMANCE)
}

/**
 * Eventually will be external once implemented in browsers
 */
enum class GPUPowerPreference {
    LOW_POWER, HIGH_PERFORMANCE
}

actual class Device(val jsType: GPUDevice) {

    override fun toString(): String {
        return "Device($jsType)"
    }

    actual fun createShaderModule(data: ByteArray): ShaderModule {
        val desc = asDynamic()
        desc.code = data;

        val moduleJs = jsType.createShaderModule(desc)

        return ShaderModule(moduleJs)
    }
}

open external class GPUDevice {
    val adapter: GPUAdapter
    val extensions: List<GPUExtensionName>
    val limits: Any
    val defaultQueue: Any

    fun createShaderModule(desc: dynamic) : GPUShaderModule
}

external class GPUShaderModule : GPUObjectBase {
    val compilationInfo: Any
}

actual class ShaderModule(val jsType: GPUShaderModule){

    override fun toString(): String {
        return "ShaderModule($jsType)"
    }

}