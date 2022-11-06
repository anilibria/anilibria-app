package ru.radiationx.anilibria.presentation.auth

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView

/**
 * Created by radiationx on 30.12.17.
 */

interface Auth2FaCodeView : IBaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setSignButtonEnabled(isEnabled: Boolean)
}