import io.github.kgpu.*;

fun runWindowEventExample(window: Window){
    window.onKeyDown = { key: KeyEvent ->
        println("Key Down: $key")
    }

    window.onKeyUp = {key: KeyEvent -> 
        println("Key Up: $key")
    }

    Kgpu.runLoop(window) {

    }
}