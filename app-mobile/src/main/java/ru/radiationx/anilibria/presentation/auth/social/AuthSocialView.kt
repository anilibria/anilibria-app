package ru.radiationx.anilibria.presentation.auth.social

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.auth.social.AuthSocialScreenState
import ru.radiationx.data.entity.domain.auth.SocialAuth

@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthSocialView : MvpView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun loadPage(data: SocialAuth)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: AuthSocialScreenState)

    @StateStrategyType(SkipStrategy::class)
    fun showError()
}