package ru.radiationx.anilibria.presentation.main

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*

/**
 * Created by radiationx on 17.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
    fun setAntiDdosVisibility(isVisible: Boolean)

    fun highlightTab(screenKey: String)
    fun updateTabs()
}
