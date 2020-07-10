
import io.github.kgpu.Window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    val window = Window()
    window.setTitle("Kgpu - Web")

    GlobalScope.launch {
        runExample(window)
    }
}