package ru.radiationx.anilibria.ui.presenter.cust

import android.content.Context
import android.view.ViewGroup
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ListRowView
import androidx.leanback.widget.RowPresenter

open class CustomListRowPresenter @JvmOverloads constructor(
    focusZoomFactor: Int = FocusHighlight.ZOOM_FACTOR_MEDIUM,
    useFocusDimmer: Boolean = false,
) : ListRowPresenter(focusZoomFactor, useFocusDimmer) {

    override fun onRowViewExpanded(holder: RowPresenter.ViewHolder, expanded: Boolean) {
        super.onRowViewExpanded(holder, expanded)
        val rowHolder = holder as CustomListRowViewHolder
        updateExpandedPadding(rowHolder)
        rowHolder.isExpanded = expanded
    }

    override fun onRowViewSelected(holder: RowPresenter.ViewHolder, selected: Boolean) {
        super.onRowViewSelected(holder, selected)
        val rowHolder = holder as CustomListRowViewHolder
        updateExpandedPadding(rowHolder)
        rowHolder.isSelected = selected
    }

    private fun updateExpandedPadding(holder: CustomListRowViewHolder) {
        if (!holder.isExpanded) return
        val grid = holder.gridView
        val spaceUnderBaseline = holder.headerViewHolder?.let { header ->
            headerPresenter?.getSpaceUnderBaseline(header) ?: header.view.paddingBottom
        } ?: 0
        // Keep room for the focused poster throughout both focus animations.
        // Leanback otherwise changes this padding immediately on row selection.
        val paddingTop = grid.resources.getDimensionPixelSize(
            androidx.leanback.R.dimen.lb_browse_expanded_selected_row_top_padding
        ) - spaceUnderBaseline
        if (grid.paddingTop != paddingTop) {
            grid.setPadding(grid.paddingLeft, paddingTop, grid.paddingRight, grid.paddingBottom)
        }
    }

    override fun onSelectLevelChanged(holder: RowPresenter.ViewHolder) {
        super.onSelectLevelChanged(holder)
        (holder as CustomListRowViewHolder).updateDescriptionHeight()
    }

    override fun createRowViewHolder(parent: ViewGroup): RowPresenter.ViewHolder {
        initStatics(parent.context)
        val rowView = ListRowView(parent.context)
        setupFadingEffect(rowView)
        if (rowHeight != 0) {
            rowView.gridView.setRowHeight(rowHeight)
        }
        return CustomListRowViewHolder(rowView, rowView.gridView, this)
    }

    private fun setupFadingEffect(listRowView: ListRowView) {
        ListRowPresenter::class.java.getDeclaredMethod("setupFadingEffect", ListRowView::class.java)
            .let {
                it.isAccessible = true
                it.invoke(this, listRowView)
            }
    }

    private fun initStatics(context: Context) {
        ListRowPresenter::class.java.getDeclaredMethod("initStatics", Context::class.java)
            .let {
                it.isAccessible = true
                it.invoke(this, context)
            }
    }
}
