package ru.radiationx.anilibria.presentation.other

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.other.ProfileScreenState

@StateStrategyType(AddToEndSingleStrategy::class)
interface OtherView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: ProfileScreenState)

    @StateStrategyType(SkipStrategy::class)
    fun showOtpCode()
}
