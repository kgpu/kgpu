package io.github.kgpu

expect class ImageData{
    val width: Int
    val height: Int
    val bytes: ByteArray

    companion object{

        suspend fun load(src: String) : ImageData

    }
}