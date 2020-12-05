expect fun setExampleStatus(id: String, msg: String)

expect suspend fun flushExampleStatus()

expect suspend fun timeExecution(func: suspend () -> Unit): Long
