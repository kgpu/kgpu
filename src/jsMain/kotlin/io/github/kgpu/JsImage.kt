package io.github.kgpu

import io.github.kgpu.internal.ArrayBufferUtils
import kotlinx.coroutines.await
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.RenderingContext
import kotlin.browser.document
import kotlin.js.Promise

actual class ImageData(
    actual val width: Int,
    actual val height: Int,
    actual val bytes: ByteArray
) {

    actual companion object {
        actual suspend fun load(src: String): ImageData {
            val img = document.createElement("img") as HTMLImageElement
            img.src = src
            (img.asDynamic().decode() as Promise<dynamic>).await()

            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = img.width
            canvas.height = img.height

            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            context.translate(0.0, img.height.toDouble())
            context.scale(1.0, -1.0)
            context.drawImage(img, 0.0, 0.0, img.width.toDouble(), img.height.toDouble())
            val pixels = context.getImageData(0.0, 0.0, img.width.toDouble(), img.height.toDouble())

            return ImageData(img.width, img.height, ArrayBufferUtils.toByteArray(pixels.data.buffer))
        }

    }
}