package ru.radiationx.anilibria.ui.fragments

import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.RecyclerView

open class ToolbarShadowController(
        protected val recyclerView: androidx.recyclerview.widget.RecyclerView,
        protected val appBarLayout: AppBarLayout? = null,
        protected val visibleListener: (Boolean) -> Unit
) {

    protected var listScrollY = 0
    protected var appBarOffset = 0
    protected var lastVisible = false

    init {
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                listScrollY = recyclerView.computeVerticalScrollOffset()
                updateToolbarShadow()
            }
        })

        appBarLayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, i ->
            appBarOffset = i
            updateToolbarShadow()
        })
    }

    open fun updateToolbarShadow() {
        val isVisible = isShadowVisible()
        if (lastVisible != isVisible) {
            lastVisible = isVisible
            visibleListener.invoke(lastVisible)
        }
    }

    open fun isShadowVisible(): Boolean {
        return appBarOffset != 0 || listScrollY > 0
    }

}