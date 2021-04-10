package ru.radiationx.anilibria.presentation.donation.yoomoney

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationYooMoneyPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationYooMoneyView>(router) {

}