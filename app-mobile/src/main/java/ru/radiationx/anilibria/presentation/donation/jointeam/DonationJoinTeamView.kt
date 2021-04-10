package ru.radiationx.anilibria.presentation.donation.jointeam

import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.entity.app.donation.other.DonationInfraInfo
import ru.radiationx.data.entity.app.donation.other.DonationJoinTeamInfo

@StateStrategyType(SkipStrategy::class)
interface DonationJoinTeamView : IBaseView {

    fun showData(data: DonationJoinTeamInfo)
}