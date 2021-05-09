import boid.runBoidExample
import compute.runComputeCompareExample
import io.github.kgpu.Window
import kotlin.js.Date
import kotlin.js.Promise
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import msaa.runMsaaTriangle
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL

fun main() {
    val kgpuWindow = Window()
    kgpuWindow.setTitle("Kgpu - Web")

    val params = URL(window.location.href).searchParams

    GlobalScope.launch {
        when (params.get("example")) {
            "1" -> runCubeExample(kgpuWindow)
            "2" -> runTextureExample(kgpuWindow)
            "3" -> runEarthExample(kgpuWindow)
            "4" -> {
                hideCanvas()
                runComputeExample()
            }
            "5" -> runMsaaTriangle(kgpuWindow)
            "6" -> {
                hideCanvas()
                runComputeCompareExample()
            }
            "7" -> runWindowEventExample(kgpuWindow)
            "8" -> runBoidExample(kgpuWindow)
            else -> runTriangleExample(kgpuWindow)
        }
    }
}

fun hideCanvas() {
    document.getElementById("kgpuCanvas")?.setAttribute("hidden", "true")
}

actual fun setExampleStatus(id: String, msg: String) {
    val element = document.getElementById(id) ?: createStatusElement(id)

    element.innerHTML = "$id: $msg"
}

fun createStatusElement(id: String): Element {
    val element = document.createElement("h3") as HTMLElement
    element.id = id
    element.style.fontFamily = "monospace"
    document.getElementById("infoFields")?.append(element)

    return element
}

actual suspend fun timeExecution(func: suspend () -> Unit): Long {
    val start = Date().getTime()
    func()
    return (Date().getTime() - start).toLong()
}

actual suspend fun flushExampleStatus() {
    val promise: Promise<Unit> = Promise { resolve, reject -> window.setTimeout(resolve, 0) }

    promise.await()
}
