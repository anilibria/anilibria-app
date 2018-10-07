package ru.radiationx.anilibria.ui.widgets.bbwidgets

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.TextView
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getDrawableAttr

/**
 * Created by radiationx on 21.01.18.
 */
class BbQuote @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BbView(context, attrs, defStyleAttr) {

    init {
        val dens = resources.displayMetrics.density
        setPadding(
                (8 * dens).toInt(),
                (8 * dens).toInt(),
                (8 * dens).toInt(),
                (8 * dens).toInt()
        )
        background = context.getDrawableAttr(R.attr.bg_comment_quote)

        val title = TextView(context)
        title.text = "Цитата"
        addView(title, 0)
    }

    override fun deleteContent() {
        removeViews(1, childCount - 1)
    }
}