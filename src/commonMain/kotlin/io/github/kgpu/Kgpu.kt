package io.github.kgpu

expect object Kgpu{
    val backendName: String

    fun runLoop(window: Window, func: () -> Unit)
}

expect class Window(){

    fun setTitle(title: String)

    fun isCloseRequested() : Boolean

    fun update()

}