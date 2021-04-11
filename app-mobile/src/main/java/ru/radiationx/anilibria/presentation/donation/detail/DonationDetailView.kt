package ru.radiationx.anilibria.presentation.donation.detail

import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.donation.DonationDetail

@StateStrategyType(SkipStrategy::class)
interface DonationDetailView : IBaseView {

    fun showData(data: DonationDetail)

    fun openYooMoney()

    fun openJoinTeam()

    fun openInfra()
}