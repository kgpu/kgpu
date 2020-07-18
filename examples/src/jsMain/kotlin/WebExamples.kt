import io.github.kgpu.Kgpu
import io.github.kgpu.Window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import msaa.runMsaaTriangle
import org.w3c.dom.url.URL
import kotlin.browser.document
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
            "4" -> {
                hideCanvas()
                runComputeExample()
            }
            "5" -> runMsaaTriangle(kgpuWindow)
            else -> runTriangleExample(kgpuWindow)
        }
    }
}

actual fun showComputeExampleResults(output: String){
    window.alert(output)
}

fun hideCanvas(){
    document.getElementById("kgpuCanvas")?.setAttribute("hidden", "true")
}