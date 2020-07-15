import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL
import kotlin.browser.window

fun main(){
    Kgpu.init()

    val kgpuWindow = Window()
    kgpuWindow.setTitle("Kgpu - Web")

    val params = URL(window.location.href).searchParams;

    GlobalScope.launch {
        when(params.get("example")){
            "1" -> runCubeExample(kgpuWindow)
            "2" -> runTextureExample(kgpuWindow)
            "3" -> runEarthExample(kgpuWindow)
            else -> runTriangleExample(kgpuWindow)
        }
    }
}