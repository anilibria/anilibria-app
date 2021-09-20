package ru.radiationx.data.repository

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.datasource.remote.api.DonationApi
import ru.radiationx.data.entity.app.donation.DonationDetailResponse
import ru.radiationx.data.entity.app.donation.content_data.YooMoneyDialogResponse
import ru.radiationx.data.entity.domain.donation.DonationInfo
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog
import ru.radiationx.data.entity.mapper.toDomain
import toothpick.InjectConstructor

@InjectConstructor
class DonationRepository(
    private val donationApi: DonationApi,
    private val donationHolder: DonationHolder,
    private val schedulers: SchedulersProvider
) {

    fun requestUpdate(): Completable = donationApi
        .getDonationDetail()
        .flatMapCompletable { donationHolder.save(it) }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun observerDonationInfo(): Observable<DonationInfo> = donationHolder
        .observe()
        .map {
            Log.d("kekeke","observerDonationInfo $it")
            it.toDomain() }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun createYooMoneyPayLink(
        amount: Int,
        type: String,
        form: YooMoneyDialog.YooMoneyForm
    ): Single<String> = donationApi
        .createYooMoneyPayLink(amount, type, form)
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())
}