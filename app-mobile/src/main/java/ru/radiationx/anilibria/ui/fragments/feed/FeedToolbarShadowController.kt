package ru.radiationx.anilibria.ui.fragments.feed

import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController

class FeedToolbarShadowController(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        appBarLayout: AppBarLayout? = null,
        visibleListener: (Boolean) -> Unit
) : ToolbarShadowController(recyclerView, appBarLayout, visibleListener) {
    override fun isShadowVisible(): Boolean {
        return -appBarOffset == appBarLayout?.height ?: 0
    }
}