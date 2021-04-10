package ru.radiationx.anilibria.presentation.donation.yoomoney

import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo
import ru.radiationx.data.entity.app.donation.other.DonationInfraInfo

@StateStrategyType(SkipStrategy::class)
interface DonationYooMoneyView : IBaseView {

    fun showData(data: DonationYooMoneyInfo)
}