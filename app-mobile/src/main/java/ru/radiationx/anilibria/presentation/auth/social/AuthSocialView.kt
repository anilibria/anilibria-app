package ru.radiationx.anilibria.presentation.auth.social

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthSocialView : IBaseView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun loadPage(url: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showClearCookies()

    @StateStrategyType(SkipStrategy::class)
    fun showError()
}