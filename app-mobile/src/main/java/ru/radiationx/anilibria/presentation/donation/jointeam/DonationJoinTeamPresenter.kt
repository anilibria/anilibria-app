package ru.radiationx.anilibria.presentation.donation.jointeam

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.donation.infra.DonationInfraView
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationJoinTeamPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationJoinTeamView>(router) {

    fun onNoticeClick() {

    }

    fun onTelegramClick() {

    }
}