package ru.radiationx.shared.ktx.android

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

fun <R> Bitmap.asSoftware(block: (Bitmap) -> R): R {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && config == Bitmap.Config.HARDWARE) {
        copy(Bitmap.Config.ARGB_8888, true).use(block)
    } else {
        block.invoke(this)
    }
}

fun <R> Bitmap.use(block: (Bitmap) -> R): R {
    val result = block.invoke(this)
    this.recycle()
    return result
}

fun Bitmap.createAvatar(
    width: Int = this.width,
    height: Int = this.height,
    isCircle: Boolean
): Bitmap = if (isCircle) {
    val bitmap = scale(width, height)
    val output = createBitmap(bitmap.width, bitmap.height)
    val canvas = Canvas(output)

    val color = Color.RED
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawOval(rectF, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    output
} else {
    scale(width, height)
}


fun Bitmap.centerCrop(
    width: Int = this.width,
    height: Int = this.height,
    scaleFactor: Float = 1.0f
): Bitmap {
    val src = this
    var w = width
    var h = height
    val srcWidth = (src.width / scaleFactor).toInt()
    val srcHeight = (src.height / scaleFactor).toInt()
    w = (w / scaleFactor).toInt()
    h = (h / scaleFactor).toInt()

    if (w == srcWidth && h == srcHeight) {
        return src
    }
    val matrix = Matrix()
    val scale = Math.max(w.toFloat() / srcWidth, h.toFloat() / srcHeight)
    matrix.setScale(scale, scale)
    val srcCroppedW = Math.round(w / scale)
    val srcCroppedH = Math.round(h / scale)
    val srcX = (srcWidth * 0.5f - srcCroppedW / 2).toInt().let {
        Math.max(Math.min(it, srcWidth - srcCroppedW), 0)
    }
    val srcY = (srcHeight * 0.5f - srcCroppedH / 2).toInt().let {
        Math.max(Math.min(it, srcHeight - srcCroppedH), 0)
    }
    val overlay = createBitmap(srcCroppedW, srcCroppedH)
    overlay.eraseColor(Color.WHITE)
    val canvas = Canvas(overlay)
    canvas.translate(-srcX / scaleFactor, -srcY / scaleFactor)
    canvas.scale(1 / scaleFactor, 1 / scaleFactor)
    canvas.drawBitmap(src, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
    //src.recycle();
    return overlay
}