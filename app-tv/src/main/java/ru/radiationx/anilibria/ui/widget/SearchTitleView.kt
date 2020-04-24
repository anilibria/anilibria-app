package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import kotlinx.android.synthetic.main.view_titleview.view.*
import kotlinx.android.synthetic.main.view_search_controls.view.*
import ru.radiationx.anilibria.R

class SearchTitleView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.browseTitleViewStyle
) : BrowseTitleView(context, attrs, defStyleAttr) {

    var year: String?
        get() = searchTitleYear.getWonderText()
        set(value) = searchTitleYear.setWonderText(value)

    var season: String?
        get() = searchTitleSeason.getWonderText()
        set(value) = searchTitleSeason.setWonderText(value)

    var genre: String?
        get() = searchTitleGenre.getWonderText()
        set(value) = searchTitleGenre.setWonderText(value)

    var sort: String?
        get() = searchTitleSort.getWonderText()
        set(value) = searchTitleSort.setWonderText(value)

    var onlyCompleted: String?
        get() = searchTitleComplete.getWonderText()
        set(value) = searchTitleComplete.setWonderText(value)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_search_controls, title_controls)
        title_controls.isVisible = true
    }

    fun setYearClickListener(listener: OnClickListener?) {
        searchTitleYear.setOnClickListener(listener)
    }

    fun setSeasonClickListener(listener: OnClickListener?) {
        searchTitleSeason.setOnClickListener(listener)
    }

    fun setGenreClickListener(listener: OnClickListener?) {
        searchTitleGenre.setOnClickListener(listener)
    }

    fun setSortClickListener(listener: OnClickListener?) {
        searchTitleSort.setOnClickListener(listener)
    }

    fun setOnlyCompletedClickListener(listener: OnClickListener?) {
        searchTitleComplete.setOnClickListener(listener)
    }

    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (findFocus() == null && direction == View.FOCUS_UP && title_controls.requestFocus()) {
            return true
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    private fun TextView.getWonderText(): String? = text?.toString()

    private fun TextView.setWonderText(text: String?) {
        this.text = text
        this.isVisible = text != null
    }
}