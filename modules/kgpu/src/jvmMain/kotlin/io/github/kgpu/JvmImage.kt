package io.github.kgpu

import java.io.FileNotFoundException
import javax.imageio.ImageIO

actual class ImageData(actual val width: Int, actual val height: Int, actual val bytes: ByteArray) {

    actual companion object {
        actual val FORMAT = TextureFormat.RGBA8_UNORM_SRGB

        actual suspend fun load(src: String): ImageData {
            val inputStream =
                ImageData::class.java.getResourceAsStream(KgpuFiles.toJvmJarPath(src))
                    ?: throw FileNotFoundException("Failed to find image: $src")

            val image = ImageIO.read(inputStream)

            val bytes = ByteArray(image.width * image.height * 4)
            for (x in 0 until image.width) {
                for (y in 0 until image.height) {
                    val argb = image.getRGB(x, y)
                    val index = (x + (y * image.width)) * 4

                    bytes[index + 0] = (argb and 0x00ff0000 ushr 16).toByte() // r
                    bytes[index + 1] = (argb and 0x0000ff00 ushr 8).toByte() // g
                    bytes[index + 2] = (argb and 0x000000ff).toByte() // b
                    // Note: -0x1000000 is the Java int equivalent of 0xFF000000
                    bytes[index + 3] = (argb and -0x1000000 ushr 24).toByte() // a
                }
            }

            return ImageData(image.width, image.height, bytes)
        }
    }
}
