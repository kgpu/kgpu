import io.github.kgpu.*;

fun runWindowEventExample(window: Window){
    setExampleStatus("Last Event", "None")
    setExampleStatus("Mouse Pos", "(0, 0)")
    setExampleStatus("Last Typed", "None")

    window.onKeyDown = { event: KeyEvent ->
        setExampleStatus("Last Event", "Key Pressed ${event.key}")
    }

    window.onKeyUp = {event: KeyEvent ->
        setExampleStatus("Last Event", "Key Released ${event.key}")
    }

    window.onMouseClick = { event: ClickEvent ->
        setExampleStatus("Last Event", "Mouse Click ${event.button}")
    }

    window.onMouseRelease = { event: ClickEvent ->
        setExampleStatus("Last Event", "Mouse Released ${event.button}")
    }

    window.onMouseMove = {x: Float, y: Float ->
        setExampleStatus("Mouse Pos", "(${x}, ${y})")
    }

    window.onKeyTyped = {c: Char ->
        println("type")
        setExampleStatus("Last Typed", c.toString())
    }

    Kgpu.runLoop(window) {

    }
}