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

interface Auth2FaCodeView : IBaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setSignButtonEnabled(isEnabled: Boolean)
}