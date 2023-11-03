package ru.radiationx.shared_app.common

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import java.lang.ref.WeakReference

class CompatDrawableSpan(
    drawable: Drawable,
    @AlignmentType private val verticalAlignment: Int,
) : ReplacementSpan() {

    companion object {
        const val ALIGN_BOTTOM = 0
        const val ALIGN_BASELINE = 1
        const val ALIGN_CENTER = 2
    }

    @IntDef(value = [ALIGN_BOTTOM, ALIGN_BASELINE, ALIGN_CENTER])
    annotation class AlignmentType

    private val drawableRef: WeakReference<Drawable> = WeakReference(drawable)

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        fm: Paint.FontMetricsInt?,
    ): Int {
        val drawable = cachedDrawable ?: return 0
        val rect = drawable.bounds
        if (fm != null) {
            fm.ascent = -rect.bottom
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
        }
        return rect.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint,
    ) {
        val drawable = cachedDrawable ?: return
        canvas.save()
        var transY = bottom - drawable.bounds.bottom
        if (verticalAlignment == ALIGN_BASELINE) {
            transY -= paint.fontMetricsInt.descent
        } else if (verticalAlignment == ALIGN_CENTER) {
            transY = top + (bottom - top) / 2 - drawable.bounds.height() / 2
        }
        canvas.translate(x, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }

    private val cachedDrawable: Drawable?
        get() {
            return drawableRef.get()
        }

    override fun toString(): String {
        return "DynamicDrawableSpan{verticalAlignment=$verticalAlignment, drawable=$cachedDrawable}"
    }
}