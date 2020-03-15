package dev.rx.tvtest.cust

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.*

open class CustomListRowPresenter @JvmOverloads constructor(
    focusZoomFactor: Int = FocusHighlight.ZOOM_FACTOR_MEDIUM,
    useFocusDimmer: Boolean = false
) : ListRowPresenter(focusZoomFactor, useFocusDimmer) {

    override fun onRowViewExpanded(holder: RowPresenter.ViewHolder, expanded: Boolean) {
        super.onRowViewExpanded(holder, expanded)
        (holder as CustomListRowViewHolder).isExpanded = expanded
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

    protected fun setupFadingEffect(listRowView: ListRowView) {
        ListRowPresenter::class.java.getDeclaredMethod("setupFadingEffect", ListRowView::class.java)
            .let {
                it.isAccessible = true
                it.invoke(this, listRowView)
            }
    }

    protected fun initStatics(context: Context) {
        ListRowPresenter::class.java.getDeclaredMethod("initStatics", Context::class.java)
            .let {
                it.isAccessible = true
                it.invoke(this, context)
            }
    }
}