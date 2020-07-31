package io.github.kgpu

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document as jsDocument
import kotlin.browser.window as jsWindow

actual class Window actual constructor() {

    private val canvas = kotlin.browser.document.getElementById("kgpuCanvas") as HTMLCanvasElement
    private val context = canvas.getContext("gpupresent")
    private var canvasHackRan = false
    actual var windowSize: WindowSize = WindowSize(canvas.width, canvas.height)
        private set
    actual var onResize: (size: WindowSize) -> Unit = {}
    actual var onKeyDown: (event: KeyEvent) -> Unit = {}
    actual var onKeyUp: (key: KeyEvent) -> Unit = {}
    actual var onMouseClick: (event: ClickEvent) -> Unit = {}
    actual var onMouseRelease: (event: ClickEvent) -> Unit = {}
    actual var mousePos: Point = Point(0, 0)
        private set

    init {
        jsWindow.addEventListener("keydown", EventListener { event ->
            val keyEvent = event as KeyboardEvent

            onKeyDown(toKeyEvent(keyEvent))
        })

        jsWindow.addEventListener("keyup", EventListener { event ->
            val keyEvent = event as KeyboardEvent

            onKeyUp(toKeyEvent(keyEvent))
        })

        jsWindow.addEventListener("mousedown", EventListener { event ->
            val mouseEvent = event as MouseEvent

            if(isEventOnCanvas(mouseEvent)) {
                onMouseClick(toClickEvent(mouseEvent))
            }
        })

        jsWindow.addEventListener("mouseup", EventListener { event ->
            val mouseEvent = event as MouseEvent

            if(isEventOnCanvas(mouseEvent)) {
                onMouseRelease(toClickEvent(mouseEvent))
            }
        })

        canvas.onmousemove = { event: MouseEvent ->
            val rect = canvas.getBoundingClientRect()

            if(isEventOnCanvas(event)){
                mousePos = Point(
                    (event.clientX - rect.left).toInt(),
                    (event.clientY - rect.top).toInt())
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
        if(canvas.width != windowSize.width || canvas.height != windowSize.height){
            windowSize = WindowSize(canvas.width, canvas.height)
            onResize(windowSize)
        }
    }

    actual fun configureSwapChain(desc: SwapChainDescriptor): SwapChain {
        if (!canvasHackRan) {
            canvas.width += 1 //Hack to get around chromium not showing canvas unless clicked/resized
            canvasHackRan = true
        }

        return SwapChain(context.asDynamic().configureSwapChain(desc) as GPUSwapChain)
    }

    actual fun resize(width: Int, height: Int){
        canvas.width = width;
        canvas.height = height;

        update();
    }

    private fun isEventOnCanvas(event: MouseEvent) : Boolean {
        val rect = canvas.getBoundingClientRect()
        val x = event.pageX
        val y = event.pageY

        //Do not trigger if clicked on padding/border
        return rect.left < x && rect.right > x && rect.top < y && rect.bottom > y
    }

}
private fun toClickEvent(event: MouseEvent) : ClickEvent{
    return ClickEvent(
        when(event.button.toInt()){
            0 -> MouseButton.LEFT
            1 -> MouseButton.MIDDLE
            2 -> MouseButton.RIGHT
            else -> MouseButton.UNKNOWN
        }, 
        event.shiftKey, 
        event.ctrlKey, 
        event.altKey
    )
}

private fun toKeyEvent(event: KeyboardEvent) : KeyEvent {
    val key = when(event.keyCode){
        37 -> Key.LEFT_ARROW
        38 -> Key.UP_ARROW
        39 -> Key.RIGHT_ARROW
        40 -> Key.DOWN_ARROW
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
        else -> Key.UNKNOWN
    }

    return KeyEvent(key, event.shiftKey, event.ctrlKey, event.altKey)
}