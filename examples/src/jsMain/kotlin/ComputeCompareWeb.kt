package compute;

import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.js.Date
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlinx.coroutines.await

suspend actual fun computeSetStatus(id: String, msg: String){
    val element = document.getElementById(id) ?: createStatusElement(id)

    element.innerHTML = "$id: $msg"

    yieldToBrowser()
}

fun createStatusElement(id: String) : Element{
    val element = document.createElement("h3") as HTMLElement
    element.id = id
    element.style.fontFamily = "monospace"
    document.getElementById("infoFields")?.append(element)

    return element    
}

suspend actual fun timeExecution(func: suspend () -> Unit)  : Long{
    val start = Date().getTime()
    func()
    return (Date().getTime() - start).toLong()
}

suspend fun yieldToBrowser() {
    val promise : Promise<Unit> = Promise({resolve, reject -> 
        window.setTimeout(resolve, 0)
    })

    promise.await()
}