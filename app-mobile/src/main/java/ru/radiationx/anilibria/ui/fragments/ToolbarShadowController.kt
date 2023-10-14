package ru.radiationx.anilibria.ui.fragments

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

open class ToolbarShadowController(
    protected val recyclerView: RecyclerView,
    protected val appBarLayout: AppBarLayout? = null,
    protected val visibleListener: (Boolean) -> Unit,
) {

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            listScrollY = recyclerView.computeVerticalScrollOffset()
            updateToolbarShadow()
        }
    }

    private val offsetListener = AppBarLayout.OnOffsetChangedListener { _, i ->
        appBarOffset = i
        updateToolbarShadow()
    }

    protected var listScrollY = 0
    protected var appBarOffset = 0
    protected var lastVisible = false

    init {
        recyclerView.doOnAttach {
            recyclerView.addOnScrollListener(scrollListener)
            appBarLayout?.addOnOffsetChangedListener(offsetListener)
        }

        recyclerView.doOnDetach {
            recyclerView.removeOnScrollListener(scrollListener)
            appBarLayout?.removeOnOffsetChangedListener(offsetListener)
        }
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