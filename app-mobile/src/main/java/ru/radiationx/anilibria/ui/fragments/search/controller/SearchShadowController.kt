package ru.radiationx.anilibria.ui.fragments.search.controller

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView

open class SearchShadowController(
    protected val recyclerView: RecyclerView,
    protected val visibleListener: (Boolean) -> Unit,
) {

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            listScrollY = recyclerView.computeVerticalScrollOffset()
            updateToolbarShadow()
        }
    }

    private var listScrollY = 0
    private var lastVisible = false

    init {
        recyclerView.doOnAttach {
            recyclerView.addOnScrollListener(scrollListener)
        }

        recyclerView.doOnDetach {
            recyclerView.removeOnScrollListener(scrollListener)
        }
    }

    private fun updateToolbarShadow() {
        val isVisible = isShadowVisible()
        if (lastVisible != isVisible) {
            lastVisible = isVisible
            visibleListener.invoke(lastVisible)
        }
    }

    private fun isShadowVisible(): Boolean {
        return listScrollY > 0
    }
}