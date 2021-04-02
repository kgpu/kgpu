package io.github.kgpu

import io.github.kgpu.wgpuj.wgpu_h
import jdk.incubator.foreign.MemoryAddress
import java.nio.IntBuffer
import org.lwjgl.glfw.*
import org.lwjgl.system.JNI.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.macosx.ObjCRuntime
import org.lwjgl.system.macosx.ObjCRuntime.*

actual class Window actual constructor() {
    private val handle: Long = GLFW.glfwCreateWindow(640, 480, "", MemoryUtil.NULL, MemoryUtil.NULL)
    internal val surface: MemoryAddress
    actual var windowSize: WindowSize = WindowSize(0, 0)
        private set
    actual var onResize: (size: WindowSize) -> Unit = {}
    actual var onKeyDown: (key: KeyEvent) -> Unit = {}
    actual var onKeyUp: (key: KeyEvent) -> Unit = {}
    actual var onKeyTyped: (c: Char) -> Unit = {}
    actual var onMouseClick: (event: ClickEvent) -> Unit = {}
    actual var onMouseRelease: (event: ClickEvent) -> Unit = {}
    actual var onMouseMove: (x: Float, y: Float) -> Unit = { _, _ -> }
    actual var mouseX = 0f
        private set
    actual var mouseY = 0f
        private set

    init {
        val osHandle = GlfwHandler.getOsWindowHandle(handle)
        surface =
            when {
                Platform.isWindows -> {
                    val desc = wgpu_h.WGPUSurfaceDescriptor.allocate()
                    val windowsDesc = wgpu_h.WGPUSurfaceDescriptorFromWindowsHWND.allocate()
                    wgpu_h.WGPUSurfaceDescriptorFromWindowsHWND.`hinstance$set`(
                        windowsDesc,
                        MemoryAddress.ofLong(osHandle)
                    )
                    wgpu_h.WGPUChainedStruct.`sType$set`(
                        wgpu_h.WGPUSurfaceDescriptorFromWindowsHWND.`chain$slice`(
                            windowsDesc
                        ), wgpu_h.WGPUSType_SurfaceDescriptorFromWindowsHWND()
                    )
                    wgpu_h.WGPUSurfaceDescriptor.`label$set`(desc, CUtils.NULL)
                    wgpu_h.WGPUSurfaceDescriptor.`nextInChain$set`(desc, windowsDesc.address())

                    wgpu_h.wgpuInstanceCreateSurface(CUtils.NULL, desc.address())
                }
                Platform.isLinux -> {
                    //                val display = GLFWNativeX11.glfwGetX11Display()
                    //                WgpuJava.wgpuNative.wgpu_create_surface_from_xlib(display, osHandle)
                    TODO()
                }
                Platform.isMac -> {
                    //                val objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend")
                    //                val CAMetalLayer = objc_getClass("CAMetalLayer")
                    //                val contentView = invokePPP(osHandle, sel_getUid("contentView"), objc_msgSend)
                    //                // [ns_window.contentView setWantsLayer:YES];
                    //                invokePPV(contentView, sel_getUid("setWantsLayer:"), true, objc_msgSend)
                    //                // metal_layer = [CAMetalLayer layer];
                    //                val metal_layer = invokePPP(CAMetalLayer, sel_registerName("layer"), objc_msgSend)
                    //                // [ns_window.contentView setLayer:metal_layer];
                    //                invokePPPP(contentView, sel_getUid("setLayer:"), metal_layer, objc_msgSend)
                    //                WgpuJava.wgpuNative.wgpu_create_surface_from_metal_layer(metal_layer)
                    TODO()
                }
                else -> {
                    println(
                        "[WARNING] Platform not tested. See " + "https://github.com/kgpu/kgpu/issues/1"
                    )
                    CUtils.NULL
                }
            }
        if (handle == MemoryUtil.NULL)
            throw java.lang.RuntimeException("Failed to create the window!")

        windowSize = GlfwHandler.getWindowDimension(handle)
        GlfwHandler.centerWindow(handle)

        GLFW.glfwSetWindowSizeCallback(handle) { _, width, height ->
            windowSize = WindowSize(width, height)
            onResize(windowSize)
        }

        GLFW.glfwSetKeyCallback(handle) { _, keyCode, _, action, mod ->
            val kgpuKey = glfwKeyToKgpuKey(keyCode)
            val shift = (mod and 1) != 0
            val ctrl = (mod and 2) != 0
            val alt = (mod and 4) != 0

            val event = KeyEvent(kgpuKey, shift, ctrl, alt)
            when (action) {
                GLFW.GLFW_PRESS, GLFW.GLFW_REPEAT -> onKeyDown(event)
                GLFW.GLFW_RELEASE -> onKeyUp(event)
            }
        }

        GLFW.glfwSetMouseButtonCallback(handle) { _, button, action, mod ->
            val shift = (mod and 1) != 0
            val ctrl = (mod and 2) != 0
            val alt = (mod and 4) != 0

            val event =
                ClickEvent(
                    when (button) {
                        0 -> MouseButton.LEFT
                        1 -> MouseButton.RIGHT
                        2 -> MouseButton.MIDDLE
                        else -> MouseButton.UNKNOWN
                    },
                    shift,
                    ctrl,
                    alt
                )
            when (action) {
                GLFW.GLFW_PRESS -> onMouseClick(event)
                GLFW.GLFW_RELEASE -> onMouseRelease(event)
            }
        }

        GLFW.glfwSetCharCallback(handle) { _, codepoint -> onKeyTyped(codepoint.toChar()) }

        GLFW.glfwSetCursorPosCallback(handle) { _, x, y ->
            mouseX = x.toFloat()
            mouseY = y.toFloat()
            onMouseMove(mouseX, mouseY)
        }
    }

    actual fun setTitle(title: String) {
        GLFW.glfwSetWindowTitle(handle, title)
    }

    actual fun isCloseRequested(): Boolean {
        return GLFW.glfwWindowShouldClose(handle)
    }

    actual fun update() {
        GLFW.glfwPollEvents()
    }

    actual fun configureSwapChain(desc: SwapChainDescriptor): SwapChain {
        TODO()
    }

    actual fun resize(width: Int, height: Int) {
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

        // Do not use opengl
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
    }

    fun centerWindow(handle: Long) {
        val currentDimension = getWindowDimension(handle)
        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

        // Center the window
        GLFW.glfwSetWindowPos(
            handle,
            ((vidmode!!.width() - currentDimension.width) / 2).toInt(),
            ((vidmode.height() - currentDimension.height) / 2).toInt()
        )
    }

    fun getWindowDimension(handle: Long): WindowSize {
        return MemoryStack.stackPush().use { stack: MemoryStack ->
            val width: IntBuffer = stack.mallocInt(1)
            val height: IntBuffer = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(handle, width, height)

            WindowSize(width.get(), height.get())
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
            Platform.isMac -> {
                GLFWNativeCocoa.glfwGetCocoaWindow(handle)
            }
            else -> {
                println("Warning: Could not determine OS handle")
                0
            }
        }
    }
}

private fun glfwKeyToKgpuKey(glfwKey: Int): Key {
    return when (glfwKey) {
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
        GLFW.GLFW_KEY_0 -> Key.DIGIT_0
        GLFW.GLFW_KEY_1 -> Key.DIGIT_1
        GLFW.GLFW_KEY_2 -> Key.DIGIT_2
        GLFW.GLFW_KEY_3 -> Key.DIGIT_3
        GLFW.GLFW_KEY_4 -> Key.DIGIT_4
        GLFW.GLFW_KEY_5 -> Key.DIGIT_5
        GLFW.GLFW_KEY_6 -> Key.DIGIT_6
        GLFW.GLFW_KEY_7 -> Key.DIGIT_7
        GLFW.GLFW_KEY_8 -> Key.DIGIT_8
        GLFW.GLFW_KEY_9 -> Key.DIGIT_9
        GLFW.GLFW_KEY_TAB -> Key.TAB
        GLFW.GLFW_KEY_LEFT_SHIFT -> Key.SHIFT
        GLFW.GLFW_KEY_LEFT_CONTROL -> Key.CTRL
        GLFW.GLFW_KEY_LEFT_ALT -> Key.ALT
        GLFW.GLFW_KEY_ESCAPE -> Key.ESCAPE
        GLFW.GLFW_KEY_F1 -> Key.F1
        GLFW.GLFW_KEY_F2 -> Key.F2
        GLFW.GLFW_KEY_F3 -> Key.F3
        GLFW.GLFW_KEY_F4 -> Key.F4
        GLFW.GLFW_KEY_F5 -> Key.F5
        GLFW.GLFW_KEY_F6 -> Key.F6
        GLFW.GLFW_KEY_F7 -> Key.F7
        GLFW.GLFW_KEY_F8 -> Key.F8
        GLFW.GLFW_KEY_F9 -> Key.F9
        GLFW.GLFW_KEY_F10 -> Key.F10
        GLFW.GLFW_KEY_F11 -> Key.F11
        GLFW.GLFW_KEY_F12 -> Key.F12
        GLFW.GLFW_KEY_MINUS -> Key.MINUS
        GLFW.GLFW_KEY_EQUAL -> Key.EQUAL
        GLFW.GLFW_KEY_LEFT_BRACKET -> Key.LEFT_BRACKET
        GLFW.GLFW_KEY_RIGHT_BRACKET -> Key.RIGHT_BRACKET
        GLFW.GLFW_KEY_BACKSLASH -> Key.BACKSLASH
        GLFW.GLFW_KEY_SEMICOLON -> Key.SEMICOLON
        GLFW.GLFW_KEY_APOSTROPHE -> Key.APOSTROPHE
        GLFW.GLFW_KEY_COMMA -> Key.COMMA
        GLFW.GLFW_KEY_PERIOD -> Key.PERIOD
        GLFW.GLFW_KEY_SLASH -> Key.SLASH
        GLFW.GLFW_KEY_ENTER -> Key.ENTER
        GLFW.GLFW_KEY_BACKSPACE -> Key.BACKSPACE
        GLFW.GLFW_KEY_GRAVE_ACCENT -> Key.ACCENT_GRAVE
        GLFW.GLFW_KEY_CAPS_LOCK -> Key.CAPS_LOCK
        GLFW.GLFW_KEY_SPACE -> Key.SPACE
        GLFW.GLFW_KEY_DELETE -> Key.DELETE
        else -> Key.UNKNOWN
    }
}
