package io.github.kgpu

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWNativeWin32
import org.lwjgl.glfw.GLFWNativeX11
import org.lwjgl.glfw.GLFWWindowSizeCallbackI
import org.lwjgl.glfw.GLFWKeyCallbackI
import org.lwjgl.glfw.GLFWMouseButtonCallbackI
import org.lwjgl.glfw.GLFWCursorPosCallbackI
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import io.github.kgpu.wgpuj.WgpuJava
import io.github.kgpu.wgpuj.jni.*
import io.github.kgpu.wgpuj.util.Platform

actual class Window actual constructor() {
    private val handle: Long = GLFW.glfwCreateWindow(640, 480, "", MemoryUtil.NULL, MemoryUtil.NULL);
    internal val surface: Long
    actual var windowSize: WindowSize = WindowSize(0, 0)
        private set
    actual var onResize: (size: WindowSize) -> Unit = {}
    actual var onKeyDown: (key: KeyEvent) -> Unit = {}
    actual var onKeyUp: (key: KeyEvent) -> Unit = {}
    actual var onMouseClick: (event: ClickEvent) -> Unit = {}
    actual var onMouseRelease: (event: ClickEvent) -> Unit = {}
    actual var mousePos: Point = Point(0, 0)
        private set

    init {
        val osHandle = GlfwHandler.getOsWindowHandle(handle)
        surface = if (Platform.isWindows) {
            WgpuJava.wgpuNative.wgpu_create_surface_from_windows_hwnd(WgpuJava.createNullPointer(), osHandle)
        } else if (Platform.isLinux) {
            val display = GLFWNativeX11.glfwGetX11Display()
            WgpuJava.wgpuNative.wgpu_create_surface_from_xlib(display, osHandle)
        } else {
            println("[WARNING] Platform not tested. See " +
                    "https://github.com/DevOrc/wgpu-java/issues/4")
            0
        }

        if (handle == MemoryUtil.NULL)
            throw java.lang.RuntimeException("Failed to create the window!")

        windowSize = GlfwHandler.getWindowDimension(handle) 
        GlfwHandler.centerWindow(handle)

        GLFW.glfwSetWindowSizeCallback(handle, GLFWWindowSizeCallbackI { window, width, height ->
            windowSize = WindowSize(width, height)
            onResize(windowSize)
        })

        GLFW.glfwSetKeyCallback(handle, GLFWKeyCallbackI { window, keyCode, scancode, action, mod ->
            val kgpuKey = glfwKeyToKgpuKey(keyCode)
            val shift = (mod and 1) != 0
            val ctrl = (mod and 2) != 0
            val alt = (mod and 4) != 0

            val event = KeyEvent(kgpuKey, shift, ctrl, alt)
            when(action) {
                GLFW.GLFW_PRESS, GLFW.GLFW_REPEAT -> onKeyDown(event)
                GLFW.GLFW_RELEASE -> onKeyUp(event)
            }
        })

        GLFW.glfwSetMouseButtonCallback(handle, GLFWMouseButtonCallbackI { window, button, action, mod ->  
            val shift = (mod and 1) != 0
            val ctrl = (mod and 2) != 0
            val alt = (mod and 4) != 0

            val event = ClickEvent(
                when(button){
                    0 -> MouseButton.LEFT
                    1 -> MouseButton.RIGHT
                    2 -> MouseButton.MIDDLE
                    else -> MouseButton.UNKNOWN
                }, 
                shift, 
                ctrl, 
                alt
            )
            when(action){
                GLFW.GLFW_PRESS -> onMouseClick(event)
                GLFW.GLFW_RELEASE -> onMouseRelease(event)
            }
        })

        GLFW.glfwSetCursorPosCallback(handle, GLFWCursorPosCallbackI { window, x, y ->
            mousePos = Point(x.toInt(), y.toInt())
        })
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

    actual fun configureSwapChain(desc: SwapChainDescriptor): SwapChain {
        val nativeDesc = WgpuSwapChainDescriptor.createDirect();
        nativeDesc.format = desc.format
        nativeDesc.usage = desc.usage
        nativeDesc.presentMode = WgpuPresentMode.FIFO
        nativeDesc.width = windowSize.width.toLong()
        nativeDesc.height = windowSize.height.toLong()

        val id = WgpuJava.wgpuNative.wgpu_device_create_swap_chain(desc.device.id, surface, nativeDesc.pointerTo)

        return SwapChain(id, this)
    }

    actual fun resize(width: Int, height: Int){
        GLFW.glfwSetWindowSize(handle, width, height)

        update()
    }
}

internal object GlfwHandler {

    fun glfwInit() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit()) {
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
            ((vidmode!!.width() - currentDimension.width) / 2).toInt(),
            ((vidmode.height() - currentDimension.height) / 2).toInt()
        )
    }

    fun getWindowDimension(handle: Long): WindowSize {
        MemoryStack.stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(handle, width, height)

            return WindowSize(width.get(), height.get())
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
                0
            }
        }
    }
}

private fun glfwKeyToKgpuKey(glfwKey: Int) : Key {
    return when(glfwKey) {
        GLFW.GLFW_KEY_A -> Key.A
        GLFW.GLFW_KEY_B -> Key.B
        GLFW.GLFW_KEY_C -> Key.C
        GLFW.GLFW_KEY_D -> Key.D
        GLFW.GLFW_KEY_E -> Key.E 
        GLFW.GLFW_KEY_F -> Key.F
        GLFW.GLFW_KEY_G -> Key.G 
        GLFW.GLFW_KEY_H -> Key.H 
        GLFW.GLFW_KEY_I -> Key.I 
        GLFW.GLFW_KEY_J -> Key.J 
        GLFW.GLFW_KEY_K -> Key.K
        GLFW.GLFW_KEY_L -> Key.L 
        GLFW.GLFW_KEY_M -> Key.M
        GLFW.GLFW_KEY_N -> Key.N
        GLFW.GLFW_KEY_O -> Key.O 
        GLFW.GLFW_KEY_P -> Key.P 
        GLFW.GLFW_KEY_Q -> Key.Q 
        GLFW.GLFW_KEY_R -> Key.R 
        GLFW.GLFW_KEY_S -> Key.S
        GLFW.GLFW_KEY_T -> Key.T 
        GLFW.GLFW_KEY_U -> Key.U 
        GLFW.GLFW_KEY_V -> Key.V 
        GLFW.GLFW_KEY_W -> Key.W 
        GLFW.GLFW_KEY_X -> Key.X 
        GLFW.GLFW_KEY_Y -> Key.Y 
        GLFW.GLFW_KEY_Z -> Key.Z
        GLFW.GLFW_KEY_LEFT -> Key.LEFT_ARROW
        GLFW.GLFW_KEY_RIGHT -> Key.RIGHT_ARROW
        GLFW.GLFW_KEY_UP -> Key.UP_ARROW 
        GLFW.GLFW_KEY_DOWN -> Key.DOWN_ARROW
        else -> Key.UNKNOWN
    }
}