package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ViewSearchControlsBinding

class SearchTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.browseTitleViewStyle
) : BrowseTitleView(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewSearchControlsBinding


    var year: String?
        get() = binding.searchTitleYear.getWonderText()
        set(value) = binding.searchTitleYear.setWonderText(value)

    var season: String?
        get() = binding.searchTitleSeason.getWonderText()
        set(value) = binding.searchTitleSeason.setWonderText(value)

    var genre: String?
        get() = binding.searchTitleGenre.getWonderText()
        set(value) = binding.searchTitleGenre.setWonderText(value)

    var sort: String?
        get() = binding.searchTitleSort.getWonderText()
        set(value) = binding.searchTitleSort.setWonderText(value)

    var onlyCompleted: String?
        get() = binding.searchTitleComplete.getWonderText()
        set(value) = binding.searchTitleComplete.setWonderText(value)

    init {
        binding =
            ViewSearchControlsBinding.inflate(LayoutInflater.from(context), getControls(), true)
        getControls().isVisible = true
    }

    fun setYearClickListener(listener: OnClickListener?) {
        binding.searchTitleYear.setOnClickListener(listener)
    }

    fun setSeasonClickListener(listener: OnClickListener?) {
        binding.searchTitleSeason.setOnClickListener(listener)
    }

    fun setGenreClickListener(listener: OnClickListener?) {
        binding.searchTitleGenre.setOnClickListener(listener)
    }

    fun setSortClickListener(listener: OnClickListener?) {
        binding.searchTitleSort.setOnClickListener(listener)
    }

    fun setOnlyCompletedClickListener(listener: OnClickListener?) {
        binding.searchTitleComplete.setOnClickListener(listener)
    }

    override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        if (findFocus() == null && direction == View.FOCUS_UP && getControls().requestFocus()) {
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