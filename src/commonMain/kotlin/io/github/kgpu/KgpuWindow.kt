package io.github.kgpu

/**
 * Represents a cross platform window. On the JVM, this window is managed by the 
 * GLFW windowing library. On the web, this window represents a canvas.
 */
expect class Window() {
    var windowSize: WindowSize
    var onResize: (size: WindowSize) -> Unit
    var onKeyDown: (event: KeyEvent) -> Unit
    var onKeyUp: (event: KeyEvent) -> Unit
    var onMouseClick: (event: ClickEvent) -> Unit
    var onMouseRelease: (event: ClickEvent) -> Unit

    fun setTitle(title: String)

    /**
     * On the desktop, this returns true when the close button has been pressed. On 
     * the web, this always returns false. 
     */
    fun isCloseRequested(): Boolean

    /**
     * On the desktop, it will poll the events for the window. On the web, 
     * it will update the window size. This function is automatically called by Kgpu.runLoop {}
     */
    fun update()

    fun configureSwapChain(desc: SwapChainDescriptor): SwapChain

    /**
     * Sets the size and updates the window
     */
    fun resize(width: Int, height: Int)
}

data class WindowSize(val width: Int, val height: Int) {
    override fun toString(): String {
        return "WindowSize($width, $height)"
    }
}

data class KeyEvent(val key: Key, val shift: Boolean, val ctrl: Boolean, val alt: Boolean){
    override fun toString(): String {
        return "KeyEvent(key = $key, shift = $shift, ctrl = $ctrl, alt = $alt)"
    }
}

data class ClickEvent(val mouse: MouseButton, val shift: Boolean, val ctrl: Boolean, val alt: Boolean)

enum class MouseButton {
    LEFT, MIDDLE, RIGHT, UNKNOWN
}

enum class Key {
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
    J, 
    K,
    L,
    M,
    N,
    O,
    P,
    Q,
    R,
    S,
    T,
    U,
    V,
    W,
    X,
    Y,
    Z,
    LEFT_ARROW,
    UP_ARROW,
    RIGHT_ARROW,
    DOWN_ARROW,
    UNKNOWN
}