
import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.runBlocking

fun main() {
    Kgpu.init();

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    runBlocking {
        runExample(window)
    }
}
