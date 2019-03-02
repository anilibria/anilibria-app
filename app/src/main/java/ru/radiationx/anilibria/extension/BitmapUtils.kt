package ru.radiationx.anilibria.extension

import android.graphics.*

fun Bitmap.createAvatar(width: Int = this.width, height: Int = this.height, isCircle: Boolean): Bitmap = if (isCircle) {
    val bitmap = Bitmap.createScaledBitmap(this, width, height, true)
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
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
    Bitmap.createScaledBitmap(this, width, height, true)
}


fun Bitmap.centerCrop(width: Int = this.width, height: Int = this.height, scaleFactor: Float = 1.0f): Bitmap {
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
    val overlay = Bitmap.createBitmap(srcCroppedW, srcCroppedH, Bitmap.Config.ARGB_8888)
    overlay.eraseColor(Color.WHITE)
    val canvas = Canvas(overlay)
    canvas.translate(-srcX / scaleFactor, -srcY / scaleFactor)
    canvas.scale(1 / scaleFactor, 1 / scaleFactor)
    canvas.drawBitmap(src, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
    //src.recycle();
    return overlay
}