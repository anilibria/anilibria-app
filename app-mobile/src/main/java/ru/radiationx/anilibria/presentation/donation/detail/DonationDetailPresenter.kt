package ru.radiationx.anilibria.presentation.donation.detail

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationDetailView>(router) {

    fun onPatreonClick() {

    }

    fun onYooMoneyClick() {

    }

    fun onDonationAlertsClick() {

    }

    fun onJoinTeamClick() {

    }

    fun onInfraClick() {

    }
}