import io.github.kgpu.Kgpu;
import io.github.kgpu.Window

fun main(){
    Kgpu.init();

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    Kgpu.runLoop(window) {

    }
}
