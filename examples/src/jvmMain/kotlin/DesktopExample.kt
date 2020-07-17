
import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Args: ${args.joinToString()}")

    val arg = if(args.size > 0){
        args[0]
    } else {
        "triangle"
    }

    runBlocking {
        when(arg){
            "-triangle" -> runTriangleExample(createWindow())
            "-cube" -> runCubeExample(createWindow())
            "-texture" -> runTextureExample(createWindow())
            "-earth" -> runEarthExample(createWindow())
            "-compute" -> {
                Kgpu.init(false)
                runComputeExample()
            }
            else -> throw RuntimeException("Unknown example: $arg");
        }
    }
}

private fun createWindow() : Window {
    Kgpu.init(true);

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    return window
}

actual fun showComputeExampleResults(output: String) {
    println(output)
}