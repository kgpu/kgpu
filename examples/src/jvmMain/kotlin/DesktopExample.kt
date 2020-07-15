
import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    println("Args: ${args.joinToString()}")
    Kgpu.init();

    val window = Window()
    window.setTitle("Kgpu - Desktop")

    val arg = if(args.size > 0){
        args[0]
    } else {
        "triangle"
    }

    runBlocking {
        when(arg){
            "-triangle" -> runTriangleExample(window)
            "-cube" -> runCubeExample(window)
            "-texture" -> runTextureExample(window)
            "-earth" -> runEarthExample(window)
            else -> throw RuntimeException("Unknown example: $arg");
        }
    }
}
