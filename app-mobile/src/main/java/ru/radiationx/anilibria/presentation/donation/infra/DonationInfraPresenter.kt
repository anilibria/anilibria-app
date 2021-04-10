package ru.radiationx.anilibria.presentation.donation.infra

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationInfraPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationInfraView>(router) {

    fun onTelegramClick() {

    }

}