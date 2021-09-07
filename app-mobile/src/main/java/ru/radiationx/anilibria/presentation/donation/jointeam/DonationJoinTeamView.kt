package ru.radiationx.anilibria.presentation.donation.jointeam

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.entity.app.donation.other.DonationInfraInfo
import ru.radiationx.data.entity.app.donation.other.DonationJoinTeamInfo

interface DonationJoinTeamView : IBaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showData(data: DonationJoinTeamInfo)
}