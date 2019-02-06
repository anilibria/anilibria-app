package ru.radiationx.anilibria.presentation.main

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder

/**
 * Created by radiationx on 17.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
    fun setAntiDdosVisibility(isVisible: Boolean)

    fun highlightTab(screenKey: String)
    fun updateTabs()
    fun onMainLogicCompleted()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun changeTheme(appTheme: AppThemeHolder.AppTheme)
}
