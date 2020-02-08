package ru.radiationx.anilibria.ui.widgets

import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import ru.radiationx.anilibria.App

class UniversalItemDecoration : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
    private var manager: androidx.recyclerview.widget.GridLayoutManager? = null
    private var spanCount: Int = 1
    private var fullWidth: Boolean = false
    private var includeEdge: Boolean = true
    private var spacing: Int = 0

    fun manager(manager: androidx.recyclerview.widget.GridLayoutManager): UniversalItemDecoration {
        this.manager = manager
        return this
    }

    fun fullWidth(fullWidth: Boolean): UniversalItemDecoration {
        this.fullWidth = fullWidth
        return this
    }

    fun includeEdge(includeEdge: Boolean): UniversalItemDecoration {
        this.includeEdge = includeEdge
        return this
    }

    fun spacing(spacing: Int): UniversalItemDecoration {
        this.spacing = spacing
        return this
    }

    fun spacingDp(spacing: Float): UniversalItemDecoration {
        this.spacing = (App.instance.resources.displayMetrics.density * spacing).toInt()
        return this
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        manager?.let {
            spanCount = it.spanCount
        }

        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        if (includeEdge) {
            if (!fullWidth) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
            }
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            if (!fullWidth) {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            }
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}
