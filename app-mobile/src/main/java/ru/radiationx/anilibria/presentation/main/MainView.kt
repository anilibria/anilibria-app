package ru.radiationx.anilibria.presentation.main

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.datasource.holders.AppThemeHolder

/**
 * Created by radiationx on 17.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
    fun highlightTab(screenKey: String)
    fun updateTabs()
    fun onMainLogicCompleted()

    fun showConfiguring()
    fun hideConfiguring()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun changeTheme(appTheme: AppThemeHolder.AppTheme)
}
