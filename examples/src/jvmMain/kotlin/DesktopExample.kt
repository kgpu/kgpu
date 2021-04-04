import boid.runBoidExample
import compute.runComputeCompareExample
import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.runBlocking
import msaa.runMsaaTriangle

fun main(args: Array<String>) {
    // This is required for macOS else things will hang on anything texture related
    // See https://github.com/LWJGL/lwjgl3/issues/68#issuecomment-113727632
    System.setProperty("java.awt.headless", "true")

    println("Args: ${args.joinToString()}")

    val arg =
        if (args.isNotEmpty()) {
            args[0]
        } else {
            "triangle"
        }

    val wgpuPath = System.getenv("WGPU_NATIVE_PATH")
    if(wgpuPath != null){
        println("Loading wgpu-native from $wgpuPath")
        System.load(wgpuPath)
    } else {
        println("Extracting wgpu-native from classpath")
        Kgpu.loadNativesFromClasspath()
    }
    Kgpu.initializeLogging()

    runBlocking {
        when (arg) {
            "-triangle" -> runTriangleExample(createWindow())
            "-cube" -> runCubeExample(createWindow())
            "-texture" -> runTextureExample(createWindow())
            "-earth" -> runEarthExample(createWindow())
            "-msaa" -> runMsaaTriangle(createWindow())
            "-window" -> runWindowEventExample(createWindow())
            "-boid" -> runBoidExample(createWindow())
            "-compute" -> runComputeExample()
            "-computeCompare" -> runComputeCompareExample()
            else -> throw RuntimeException("Unknown example: $arg")
        }
    }
}

private fun createWindow(): Window {
    Kgpu.initGlfw()

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    return window
}

actual fun setExampleStatus(id: String, msg: String) {
    println("$id: $msg")
}

actual suspend fun timeExecution(func: suspend () -> Unit): Long {
    return kotlin.system.measureTimeMillis { func() }
}

actual suspend fun flushExampleStatus() {}
