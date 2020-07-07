package io.github.kgpu

import com.noahcharlton.wgpuj.WgpuJava
import com.noahcharlton.wgpuj.util.SharedLibraryLoader
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Dimension

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
}