package ru.radiationx.anilibria.presentation.donation.detail

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.domain.donation.DonationInfo


interface DonationDetailView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showData(data: DonationInfo)

    @StateStrategyType(SkipStrategy::class)
    fun openYooMoney()

    @StateStrategyType(SkipStrategy::class)
    fun openContentDialog(tag: String)
}