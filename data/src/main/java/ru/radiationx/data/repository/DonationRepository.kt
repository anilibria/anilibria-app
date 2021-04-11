package ru.radiationx.data.repository

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.datasource.remote.api.DonationApi
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo
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

    fun observerDonationDetail(): Observable<DonationDetail> = donationHolder
        .observe()
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun createYooMoneyPayLink(
        amount: Int,
        type: String,
        form: DonationYooMoneyInfo.YooMoneyForm
    ): Single<String> = donationApi
        .createYooMoneyPayLink(amount, type, form)
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())
}