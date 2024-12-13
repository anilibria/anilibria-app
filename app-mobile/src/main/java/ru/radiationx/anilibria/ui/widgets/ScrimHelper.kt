package ru.radiationx.anilibria.ui.widgets

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by radiationx on 26.08.17.
 */

class ScrimHelper(
    private val appBarLayout: AppBarLayout,
    private val toolbarLayout: CollapsingToolbarLayout
) {

    val scrimState = MutableStateFlow(false)

    init {
        appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            val newState = computeScrimState(verticalOffset)
            if (newState == scrimState.value) {
                return@addOnOffsetChangedListener
            }
            scrimState.value = newState
        }
    }

    private fun computeScrimState(offset: Int): Boolean {
        return appBarLayout.height + offset <= toolbarLayout.scrimVisibleHeightTrigger
    }
}
