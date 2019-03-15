package ru.radiationx.anilibria.ui.fragments.feed

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController

class FeedToolbarShadowController(
        recyclerView: RecyclerView,
        appBarLayout: AppBarLayout? = null,
        visibleListener: (Boolean) -> Unit
) : ToolbarShadowController(recyclerView, appBarLayout, visibleListener) {
    override fun isShadowVisible(): Boolean {
        return -appBarOffset == appBarLayout?.height ?: 0
    }
}