package io.github.kgpu

import kotlinx.browser.document as jsDocument
import kotlinx.browser.window as jsWindow
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

actual class Window actual constructor() {

    private val canvas = kotlinx.browser.document.getElementById("kgpuCanvas") as HTMLCanvasElement
    private val context = canvas.getContext("gpupresent")
    private var canvasHackRan = false
    actual var windowSize: WindowSize = WindowSize(canvas.width, canvas.height)
        private set
    actual var onResize: (size: WindowSize) -> Unit = {}
    actual var onKeyDown: (event: KeyEvent) -> Unit = {}
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
        jsWindow.addEventListener(
            "keydown",
            { event ->
                val keyEvent = event as KeyboardEvent

                onKeyDown(toKeyEvent(keyEvent))
            })

        jsWindow.addEventListener(
            "keypress",
            { event ->
                val keyEvent = event as KeyboardEvent

                onKeyTyped(keyEvent.key[0])
            })

        jsWindow.addEventListener(
            "keyup",
            EventListener { event ->
                val keyEvent = event as KeyboardEvent

                onKeyUp(toKeyEvent(keyEvent))
            })

        jsWindow.addEventListener(
            "mousedown",
            EventListener { event ->
                val mouseEvent = event as MouseEvent

                if (isEventOnCanvas(mouseEvent)) {
                    onMouseClick(toClickEvent(mouseEvent))
                }
            })

        jsWindow.addEventListener(
            "mouseup",
            EventListener { event ->
                val mouseEvent = event as MouseEvent

                if (isEventOnCanvas(mouseEvent)) {
                    onMouseRelease(toClickEvent(mouseEvent))
                }
            })

        canvas.onmousemove =
            { event: MouseEvent ->
                val rect = canvas.getBoundingClientRect()

                if (isEventOnCanvas(event)) {
                    mouseX = (event.clientX - rect.left).toFloat()
                    mouseY = (event.clientY - rect.top).toFloat()
                    onMouseMove(mouseX, mouseY)
                }

                asDynamic() // On mouse move requires we return a dynamic
            }
    }

    actual fun setTitle(title: String) {
        jsDocument.title = title
    }

    actual fun isCloseRequested(): Boolean {
        return false
    }

    actual fun update() {
        if (canvas.width != windowSize.width || canvas.height != windowSize.height) {
            windowSize = WindowSize(canvas.width, canvas.height)
            onResize(windowSize)
        }
    }

    actual fun configureSwapChain(desc: SwapChainDescriptor): SwapChain {
        if (!canvasHackRan) {
            canvas.width +=
                1 // Hack to get around chromium not showing canvas unless clicked/resized
            canvasHackRan = true
            windowSize = WindowSize(canvas.width, canvas.height)
        }

        return SwapChain(context.asDynamic().configureSwapChain(desc) as GPUSwapChain)
    }

    actual fun resize(width: Int, height: Int) {
        canvas.width = width
        canvas.height = height

        update()
    }

    private fun isEventOnCanvas(event: MouseEvent): Boolean {
        val rect = canvas.getBoundingClientRect()
        val x = event.pageX
        val y = event.pageY

        // Do not trigger if clicked on padding/border
        return rect.left < x && rect.right > x && rect.top < y && rect.bottom > y
    }
}

private fun toClickEvent(event: MouseEvent): ClickEvent {
    return ClickEvent(
        when (event.button.toInt()) {
            0 -> MouseButton.LEFT
            1 -> MouseButton.MIDDLE
            2 -> MouseButton.RIGHT
            else -> MouseButton.UNKNOWN
        },
        event.shiftKey,
        event.ctrlKey,
        event.altKey)
}

private fun toKeyEvent(event: KeyboardEvent): KeyEvent {
    val key =
        when (event.keyCode) {
            8 -> Key.BACKSPACE
            9 -> Key.TAB
            13 -> Key.ENTER
            16 -> Key.SHIFT
            17 -> Key.CTRL
            18 -> Key.ALT
            20 -> Key.CAPS_LOCK
            27 -> Key.ESCAPE
            32 -> Key.SPACE
            37 -> Key.LEFT_ARROW
            38 -> Key.UP_ARROW
            39 -> Key.RIGHT_ARROW
            40 -> Key.DOWN_ARROW
            46 -> Key.DELETE
            48 -> Key.DIGIT_0
            49 -> Key.DIGIT_1
            50 -> Key.DIGIT_2
            51 -> Key.DIGIT_3
            52 -> Key.DIGIT_4
            53 -> Key.DIGIT_5
            54 -> Key.DIGIT_6
            55 -> Key.DIGIT_7
            56 -> Key.DIGIT_8
            57 -> Key.DIGIT_9
            65 -> Key.A
            66 -> Key.B
            67 -> Key.C
            68 -> Key.D
            69 -> Key.E
            70 -> Key.F
            71 -> Key.G
            72 -> Key.H
            73 -> Key.I
            74 -> Key.J
            75 -> Key.K
            76 -> Key.L
            77 -> Key.M
            78 -> Key.N
            79 -> Key.O
            80 -> Key.P
            81 -> Key.Q
            82 -> Key.R
            83 -> Key.S
            84 -> Key.T
            85 -> Key.U
            86 -> Key.V
            87 -> Key.W
            88 -> Key.X
            89 -> Key.Y
            90 -> Key.Z
            112 -> Key.F1
            113 -> Key.F2
            114 -> Key.F3
            115 -> Key.F4
            116 -> Key.F5
            117 -> Key.F6
            118 -> Key.F7
            119 -> Key.F8
            120 -> Key.F9
            121 -> Key.F10
            122 -> Key.F11
            123 -> Key.F12
            186 -> Key.SEMICOLON
            187 -> Key.EQUAL
            188 -> Key.COMMA
            189 -> Key.MINUS
            190 -> Key.PERIOD
            191 -> Key.SLASH
            192 -> Key.ACCENT_GRAVE
            219 -> Key.LEFT_BRACKET
            220 -> Key.BACKSLASH
            221 -> Key.RIGHT_BRACKET
            222 -> Key.APOSTROPHE
            else -> Key.UNKNOWN
        }

    return KeyEvent(key, event.shiftKey, event.ctrlKey, event.altKey)
}
