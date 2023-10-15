package ru.radiationx.anilibria.ui.fragments.feed

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController

class FeedToolbarShadowController(
    recyclerView: RecyclerView,
    appBarLayout: AppBarLayout? = null,
    visibleListener: (Boolean) -> Unit,
) : ToolbarShadowController(recyclerView, appBarLayout, visibleListener) {
    override fun isShadowVisible(): Boolean {
        return -appBarOffset == (appBarLayout?.height ?: 0)
    }
}