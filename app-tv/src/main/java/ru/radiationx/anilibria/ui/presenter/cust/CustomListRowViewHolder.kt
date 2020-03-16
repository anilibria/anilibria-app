package dev.rx.tvtest.cust

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.leanback.widget.HorizontalGridView
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ListRowView
import ru.radiationx.anilibria.ui.widget.CardDescriptionView

class CustomListRowViewHolder(
    rootView: ListRowView,
    gridView: HorizontalGridView,
    presenter: ListRowPresenter
) : ListRowPresenter.ViewHolder(rootView, gridView, presenter) {

    private val cardDescriptionView = CardDescriptionView(rootView.context)

    init {
        cardDescriptionView.updatePadding(
            left = gridView.paddingLeft,
            right = gridView.paddingRight,
            bottom = gridView.paddingBottom * 5
        )
        rootView.addView(cardDescriptionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setDescription(title: CharSequence, subtitle: CharSequence) {
        cardDescriptionView.setTitle(title)
        cardDescriptionView.setSubtitle(subtitle)
    }

    fun setExpanded(expanded: Boolean) {
        cardDescriptionView.isVisible = expanded && isSelected
    }

    fun setSelected(selected: Boolean) {
        cardDescriptionView.isVisible = selected && isExpanded
    }
}