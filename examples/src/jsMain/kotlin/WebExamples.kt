import io.github.kgpu.Kgpu
import io.github.kgpu.Window

fun main(){
    val window = Window()
    window.setTitle("Kgpu - Web")

    Kgpu.runLoop(window) {
    }
}