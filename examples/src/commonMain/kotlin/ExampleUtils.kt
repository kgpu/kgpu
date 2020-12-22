import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.ColorFormat
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import io.github.kgpu.TextureFormat

expect fun setExampleStatus(id: String, msg: String)

private val IMAGE_FORMAT = ColorFormat.Mixin(32, 0, 8, 8, 8, 16, 8, 24, 8)

val TEXTURE_FORMAT = TextureFormat.RGBA8_UNORM_SRGB

suspend fun loadImage(path: String): Pair<Bitmap, ByteArray> {
    val image = resourcesVfs[path].readBitmap()

    return Pair(image, image.toBMP32().extractBytes(IMAGE_FORMAT))
}

expect suspend fun flushExampleStatus()

expect suspend fun timeExecution(func: suspend () -> Unit): Long
