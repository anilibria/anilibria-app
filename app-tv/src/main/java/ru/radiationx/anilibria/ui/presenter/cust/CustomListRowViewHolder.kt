package ru.radiationx.anilibria.ui.presenter.cust

import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.HorizontalGridView
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ListRowView
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.widget.CardDescriptionView
import kotlin.math.roundToInt

class CustomListRowViewHolder(
    rootView: ListRowView,
    gridView: HorizontalGridView,
    presenter: ListRowPresenter,
) : ListRowPresenter.ViewHolder(rootView, gridView, presenter) {

    private val cardDescriptionView =
        CardDescriptionView(rootView.context, defStyleAttr = R.attr.rowHorizontalDescriptionStyle)

    private var collapsingDescriptionHeight = 0
    private var collapsingDescriptionStartLevel = 1f

    init {
        cardDescriptionView.visibility = View.GONE
        rootView.addView(
            cardDescriptionView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun setDescription(title: CharSequence, subtitle: CharSequence) {
        cardDescriptionView.setTitle(title)
        cardDescriptionView.setSubtitle(subtitle)
    }

    fun setExpanded(expanded: Boolean) {
        if (expanded && isSelected) {
            showDescription()
        } else {
            hideDescription()
        }
    }

    fun setSelected(selected: Boolean) {
        if (selected && isExpanded) {
            showDescription()
        } else if (isExpanded && selectLevel > 0f && cardDescriptionView.height > 0) {
            collapsingDescriptionHeight = cardDescriptionView.height
            collapsingDescriptionStartLevel = selectLevel
            cardDescriptionView.visibility = View.INVISIBLE
            updateDescriptionHeight()
        } else {
            hideDescription()
        }
    }

    fun updateDescriptionHeight() {
        if (collapsingDescriptionHeight == 0) return
        // Use Leanback's existing selection animation to remove the space gradually.
        if (selectLevel <= 0f) {
            hideDescription()
        } else {
            val fraction = (selectLevel / collapsingDescriptionStartLevel).coerceIn(0f, 1f)
            val height = (collapsingDescriptionHeight * fraction).roundToInt()
            if (cardDescriptionView.layoutParams.height != height) {
                cardDescriptionView.layoutParams = cardDescriptionView.layoutParams.apply {
                    this.height = height
                }
            }
        }
    }

    private fun showDescription() {
        collapsingDescriptionHeight = 0
        cardDescriptionView.layoutParams = cardDescriptionView.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        cardDescriptionView.visibility = View.VISIBLE
    }

    private fun hideDescription() {
        collapsingDescriptionHeight = 0
        cardDescriptionView.visibility = View.GONE
        cardDescriptionView.layoutParams = cardDescriptionView.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
}
