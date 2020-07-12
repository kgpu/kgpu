
import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import io.github.kgpu.internal.glMatrix
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import io.github.kgpu.internal.mat4

fun main() {
    Kgpu.init()

    println(glMatrix.EPSILON)
    val mat4 = mat4.create()
    println(mat4)

    val window = Window()
    window.setTitle("Kgpu - Web")

    GlobalScope.launch {
        runExample(window)
    }
}