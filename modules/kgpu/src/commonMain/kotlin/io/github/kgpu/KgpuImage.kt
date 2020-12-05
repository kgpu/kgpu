package io.github.kgpu

expect class ImageData {
    val width: Int
    val height: Int
    val bytes: ByteArray

    companion object {
        /** The texture format returned by [ImageData.load] */
        val FORMAT: TextureFormat

        suspend fun load(src: String): ImageData
    }
}
