package ru.radiationx.anilibria.presentation.donation.infra

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationInfraPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationInfraView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val infraInfo = it.otherSupport?.btInfra?.info
                if (infraInfo != null) {
                    viewState.showData(infraInfo)
                }
            }, {
                it.printStackTrace()
            })
            .addToDisposable()
    }

    fun onTelegramClick() {

    }

}