package ru.radiationx.anilibria.presentation.donation.yoomoney

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.anilibria.presentation.donation.infra.DonationYooMoneyState

interface DonationYooMoneyView : IBaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showData(state: DonationYooMoneyState)

    @StateStrategyType(SkipStrategy::class)
    fun close()
}