package ru.radiationx.anilibria.ui.fragments

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView

open class ToolbarShadowController(
        val recyclerView: RecyclerView,
        val appBarLayout: AppBarLayout? = null,
        val visibleListener: (Boolean) -> Unit
) {

    private var listScrollY = 0
    private var appBarOffset = 0
    private var lastVisible = false

    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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