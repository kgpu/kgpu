import io.github.kgpu.*;

fun runWindowEventExample(window: Window){
    window.onKeyDown = { event: KeyEvent ->
        val size = window.windowSize

        when(event.key){
            Key.LEFT_ARROW -> window.resize(size.width - 5, size.height)
            Key.RIGHT_ARROW -> window.resize(size.width + 5, size.height)
            Key.DOWN_ARROW -> window.resize(size.width, size.height + 5)
            Key.UP_ARROW -> window.resize(size.width, size.height - 5)
        }
    }

    window.onKeyUp = {event: KeyEvent -> 
        println("Key Released: $event")
    }

    window.onMouseClick = { event: ClickEvent -> 
        println("Mouse Click: $event")
    }

    window.onMouseRelease = { event: ClickEvent -> 
        println("Mouse Release: $event")
    }

    Kgpu.runLoop(window) {

    }
}