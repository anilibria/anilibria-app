package ru.radiationx.anilibria.ui.widgets

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

/**
 * Created by radiationx on 26.08.17.
 */

class ScrimHelper(appBarLayout: AppBarLayout, toolbarLayout: CollapsingToolbarLayout) {
    private var scrimListener: ScrimListener? = null
    private var scrim = false

    init {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout1, verticalOffset ->
            scrimListener?.let {
                if (appBarLayout1.height + verticalOffset <= toolbarLayout.scrimVisibleHeightTrigger) {
                    if (!scrim) {
                        scrim = true
                        it.onScrimChanged(true)
                    }
                } else {
                    if (scrim) {
                        scrim = false
                        it.onScrimChanged(false)
                    }
                }
            }
        })
    }

    fun setScrimListener(scrimListener: ScrimListener?) {
        this.scrimListener = scrimListener
    }

    interface ScrimListener {
        fun onScrimChanged(scrim: Boolean)
    }
}
