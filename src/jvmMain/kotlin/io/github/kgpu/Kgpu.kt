package io.github.kgpu

import com.noahcharlton.wgpuj.WgpuJava
import com.noahcharlton.wgpuj.jni.WgpuCLimits
import com.noahcharlton.wgpuj.jni.WgpuPowerPreference
import com.noahcharlton.wgpuj.jni.WgpuRequestAdapterOptions
import com.noahcharlton.wgpuj.jni.WgpuShaderModuleDescriptor
import com.noahcharlton.wgpuj.util.Platform
import com.noahcharlton.wgpuj.util.SharedLibraryLoader
import io.github.kgpu.GlfwHandler.getOsWindowHandle
import jnr.ffi.Pointer
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWNativeWin32
import org.lwjgl.glfw.GLFWNativeX11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicLong


actual object Kgpu {
    actual val backendName: String = "Desktop"

    fun init(){
        val libraryFile = SharedLibraryLoader().load("wgpu_native")
        WgpuJava.init(libraryFile)

        GlfwHandler.glfwInit()

        println("Wgpu Version: " + WgpuJava.getWgpuNativeVersion())
        println("LWJGL Version: " + Version.getVersion())
        println("GLFW Version: ${GLFW.GLFW_VERSION_MAJOR}.${GLFW.GLFW_VERSION_MINOR}")
    }

    actual fun runLoop(window: Window, func: () -> Unit) {
        while(!window.isCloseRequested()){
            window.update()
            func()
        }

        GlfwHandler.terminate();
    }
}

actual class Window actual constructor() {
    private val handle : Long = GLFW.glfwCreateWindow(640, 480, "", MemoryUtil.NULL, MemoryUtil.NULL);

    init {
        if(handle == MemoryUtil.NULL)
            throw java.lang.RuntimeException("Failed to create the window!")

        GlfwHandler.centerWindow(handle)
    }

    actual fun setTitle(title: String) {
        GLFW.glfwSetWindowTitle(handle, title)
    }

    actual fun isCloseRequested(): Boolean {
        return GLFW.glfwWindowShouldClose(handle)
    }

    actual fun update() {
        GLFW.glfwPollEvents();
    }

    private fun createSurface(): Long {
        val osHandle = getOsWindowHandle(handle)
        if (Platform.isWindows) {
            return WgpuJava.wgpuNative.wgpu_create_surface_from_windows_hwnd(WgpuJava.createNullPointer(), osHandle)
        } else if (Platform.isLinux) {
            val display = GLFWNativeX11.glfwGetX11Display()
            return WgpuJava.wgpuNative.wgpu_create_surface_from_xlib(display, osHandle)
        }
        throw UnsupportedOperationException(
            "Platform not supported. See " +
                    "https://github.com/DevOrc/wgpu-java/issues/4"
        )
    }

    actual suspend fun requestAdapterAsync(preference: PowerPreference): Adapter {
        val adapter = AtomicLong(0)
        val defaultBackend : Int = (1 shl 1) or (1 shl 2) or (1 shl 3)
        val options = WgpuRequestAdapterOptions.createDirect()
        options.compatibleSurface = createSurface()
        options.powerPreference = preference.nativeType

        WgpuJava.wgpuNative.wgpu_request_adapter_async(
            options.pointerTo,
            defaultBackend,
            false,
            { received: Long, userData: Pointer? -> adapter.set(received) },
            WgpuJava.createNullPointer()
        )

        return Adapter(adapter.get())
    }

}

private object GlfwHandler {

    fun glfwInit(){
        GLFWErrorCallback.createPrint(System.err).set()

        if(!GLFW.glfwInit()){
            throw RuntimeException("Failed to initialize GLFW!")
        }

        //Do not use opengl
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
    }

    fun centerWindow(handle: Long) {
        val currentDimension = getWindowDimension(handle);
        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

        // Center the window
        GLFW.glfwSetWindowPos(
            handle,
            ((vidmode!!.width() - currentDimension.getWidth()) / 2).toInt(),
            ((vidmode.height() - currentDimension.getHeight()) / 2).toInt()
        )
    }

    fun getWindowDimension(handle: Long): Dimension {
        MemoryStack.stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(handle, width, height)

            return Dimension(width.get(), height.get())
        }
    }

    fun terminate() {
        GLFW.glfwTerminate()

        val callback = GLFW.glfwSetErrorCallback(null)
        callback?.free()
    }

    fun getOsWindowHandle(handle: Long): Long {
        return when {
            Platform.isWindows -> {
                GLFWNativeWin32.glfwGetWin32Window(handle)
            }
            Platform.isLinux -> {
                GLFWNativeX11.glfwGetX11Window(handle)
            }
            else -> {
                throw UnsupportedOperationException(
                    "Platform not supported. See https://github.com/DevOrc/wgpu-java/issues/4"
                )
            }
        }
    }
}

actual class Adapter(val id: Long) {

    override fun toString(): String {
        return "Adapter${Id.fromLong(id)}"
    }

    actual suspend fun requestDeviceAsync(): Device {
        val limits = WgpuCLimits.createDirect();
        limits.maxBindGroups = 4;
        val deviceId = WgpuJava.wgpuNative.wgpu_adapter_request_device(id, 0,
            limits.pointerTo, WgpuJava.createNullPointer());

        return Device(deviceId)
    }
}

actual enum class PowerPreference(val nativeType: WgpuPowerPreference) {
    LOW(WgpuPowerPreference.LOW_POWER),
    DEFAULT(WgpuPowerPreference.DEFAULT),
    PERFORMANCE(WgpuPowerPreference.HIGH_PERFORMANCE)
}

actual class Device(val id: Long) {

    override fun toString(): String {
        return "Device${Id.fromLong(id)}"
    }

    actual fun createShaderModule(data: ByteArray): ShaderModule {
        val desc = WgpuShaderModuleDescriptor.createDirect();
        val codePtr = WgpuJava.createByteArrayPointer(data)
        desc.code.bytes = codePtr;
        desc.code.length = data.size.toLong() / 4 //length is in terms of u32s

        val module = WgpuJava.wgpuNative.wgpu_device_create_shader_module(id, desc.pointerTo)

        return ShaderModule(module)
    }
}

actual class ShaderModule(val moduleId: Long) {

    override fun toString(): String {
        return "ShaderModule${Id.fromLong(moduleId)}"
    }

}