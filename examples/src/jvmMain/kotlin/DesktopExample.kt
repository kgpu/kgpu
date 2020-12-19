import boid.runBoidExample
import compute.runComputeCompareExample
import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.runBlocking
import msaa.runMsaaTriangle

fun main(args: Array<String>) {
    println("Args: ${args.joinToString()}")

    val arg =
        if (args.isNotEmpty()) {
            args[0]
        } else {
            "triangle"
        }

    runBlocking {
        when (arg) {
            "-triangle" -> runTriangleExample(createWindow())
            "-cube" -> runCubeExample(createWindow())
            "-texture" -> runTextureExample(createWindow())
            "-earth" -> runEarthExample(createWindow())
            "-msaa" -> runMsaaTriangle(createWindow())
            "-window" -> runWindowEventExample(createWindow())
            "-boid" -> runBoidExample(createWindow())
            "-compute" -> {
                Kgpu.init(false)
                runComputeExample()
            }
            "-computeCompare" -> {
                Kgpu.init(false)
                runComputeCompareExample()
            }
            else -> throw RuntimeException("Unknown example: $arg")
        }
    }
}

private fun createWindow(): Window {
    Kgpu.init(true)

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    return window
}

actual fun setExampleStatus(id: String, msg: String) {
    println("$id: $msg")
}

suspend actual fun timeExecution(func: suspend () -> Unit): Long {
    return kotlin.system.measureTimeMillis { func() }
}

actual suspend fun flushExampleStatus() {}
