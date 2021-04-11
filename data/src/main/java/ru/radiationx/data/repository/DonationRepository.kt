package ru.radiationx.data.repository

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.DonationApi
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo
import toothpick.InjectConstructor

@InjectConstructor
class DonationRepository(
    private val context: Context,
    private val donationApi: DonationApi,
    private val schedulers: SchedulersProvider
) {

    private var currentData: DonationDetail? = null

    fun observerDonationDetail(): Observable<DonationDetail> = Observable
        .fromCallable { getAssetsData() }
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

    private fun getAssetsData(): DonationDetail {
        val gson = Gson()
        val data = currentData ?: context.assets.open("donation_detail_info.json").use { stream ->
            stream.bufferedReader().use { reader ->
                gson.fromJson<DonationDetail>(reader, DonationDetail::class.java)
            }
        }
        currentData = data
        return data
    }
}