package compute

suspend actual fun computeSetStatus(id: String, msg: String){
    println("$id: $msg")
}

suspend actual fun timeExecution(func: suspend () -> Unit)  : Long{
    return kotlin.system.measureTimeMillis {
        func()
    }
}