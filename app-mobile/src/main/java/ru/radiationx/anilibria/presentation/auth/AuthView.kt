package ru.radiationx.anilibria.presentation.auth

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.auth.SocialAuth

/**
 * Created by radiationx on 30.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthView : IBaseView {
    fun setSignButtonEnabled(isEnabled: Boolean)
    @StateStrategyType(SkipStrategy::class)
    fun showRegistrationDialog()

    fun showSocial(items: List<SocialAuthItemState>)
}