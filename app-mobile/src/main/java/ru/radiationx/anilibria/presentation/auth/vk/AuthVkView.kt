package ru.radiationx.anilibria.presentation.auth.vk

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.auth.vk.AuthVkScreenState

@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthVkView : MvpView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun loadPage(url: String, resultPattern: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: AuthVkScreenState)
}