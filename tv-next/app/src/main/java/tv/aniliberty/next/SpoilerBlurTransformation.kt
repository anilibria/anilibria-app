package tv.aniliberty.next

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import coil3.size.Size
import coil3.transform.Transformation

/** Small software blur works on Android TV 8–11 too; never modifies the original bitmap. */
class SpoilerBlurTransformation : Transformation() {
    override val cacheKey = "episode-spoiler-blur-v1-96-4x2"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val scale =
            minOf(1f, 96f / input.width.coerceAtLeast(1), 96f / input.height.coerceAtLeast(1))
        val width = (input.width * scale).toInt().coerceAtLeast(1)
        val height = (input.height * scale).toInt().coerceAtLeast(1)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(output)
            .drawBitmap(input, null, Rect(0, 0, width, height), Paint(Paint.FILTER_BITMAP_FLAG))
        var pixels = IntArray(width * height)
        output.getPixels(pixels, 0, width, 0, 0, width, height)
        repeat(2) { pixels = pass(pass(pixels, width, height, true), width, height, false) }
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun pass(input: IntArray, width: Int, height: Int, horizontal: Boolean): IntArray {
        val output = IntArray(input.size)
        val radius = 4
        val diameter = radius * 2 + 1
        val lines = if (horizontal) height else width
        val length = if (horizontal) width else height
        for (line in 0 until lines) {
            var a = 0
            var r = 0
            var g = 0
            var b = 0
            fun index(position: Int): Int =
                if (horizontal) line * width + position.coerceIn(0, length - 1)
                else position.coerceIn(0, length - 1) * width + line
            fun add(pixel: Int, sign: Int) {
                a += ((pixel ushr 24) and 255) * sign
                r += ((pixel ushr 16) and 255) * sign
                g += ((pixel ushr 8) and 255) * sign
                b += (pixel and 255) * sign
            }
            for (offset in -radius..radius) add(input[index(offset)], 1)
            for (position in 0 until length) {
                output[index(position)] =
                    ((a / diameter) shl 24) or
                        ((r / diameter) shl 16) or
                        ((g / diameter) shl 8) or
                        (b / diameter)
                add(input[index(position - radius)], -1)
                add(input[index(position + radius + 1)], 1)
            }
        }
        return output
    }
}
