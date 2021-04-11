package ru.radiationx.anilibria.presentation.donation.detail

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.donation.DonationDetail


interface DonationDetailView : IBaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showData(data: DonationDetail)

    @StateStrategyType(SkipStrategy::class)
    fun openYooMoney()

    @StateStrategyType(SkipStrategy::class)
    fun openJoinTeam()

    @StateStrategyType(SkipStrategy::class)
    fun openInfra()
}