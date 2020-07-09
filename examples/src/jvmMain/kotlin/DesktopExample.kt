import io.github.kgpu.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    Kgpu.init();

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    runExample(window)
    Thread.sleep(5000)
}
