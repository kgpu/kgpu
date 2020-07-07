package io.github.kgpu

actual object Kgpu {
    actual val backendName: String = "Web"

    actual fun runLoop(window: Window, func: () -> Unit) {
        func();

        kotlin.browser.window.requestAnimationFrame {
            runLoop(window, func)
        };
    }

}

actual class Window actual constructor() {

    actual fun setTitle(title: String) {
        kotlin.browser.document.title = title
    }

    actual fun isCloseRequested(): Boolean {
        return false
    }

    actual fun update() {

    }

}