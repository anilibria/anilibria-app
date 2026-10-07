package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ScrollView
import android.widget.TextView

/** Keeps partially visible description lines out of the scroll window. */
class DescriptionScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ScrollView(context, attrs, defStyleAttr) {

    override fun dispatchDraw(canvas: Canvas) {
        val textView = getChildAt(0) as? TextView
        val textLayout = textView?.layout
        if (textView == null || textLayout == null || textLayout.lineCount == 0) {
            super.dispatchDraw(canvas)
            return
        }

        // The canvas and child bounds use content coordinates, including scrollY.
        val textTop = textView.top + textView.totalPaddingTop
        val viewportTop = scrollY + paddingTop
        val viewportBottom = scrollY + height - paddingBottom
        var firstLine = textLayout.getLineForVertical(viewportTop - textTop)
        var lastLine = textLayout.getLineForVertical(viewportBottom - textTop)
        if (textTop + textLayout.getLineTop(firstLine) < viewportTop) {
            firstLine++
        }
        if (textTop + textLayout.getLineBottom(lastLine) > viewportBottom) {
            lastLine--
        }
        if (firstLine > lastLine) {
            return
        }

        val saveCount = canvas.save()
        canvas.clipRect(
            scrollX + paddingLeft,
            maxOf(viewportTop, textTop + textLayout.getLineTop(firstLine)),
            scrollX + width - paddingRight,
            minOf(viewportBottom, textTop + textLayout.getLineBottom(lastLine)),
        )
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }
}
