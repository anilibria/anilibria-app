package ru.radiationx.data.repository

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Observable
import ru.radiationx.data.entity.app.donation.DonationDetail
import toothpick.InjectConstructor

@InjectConstructor
class DonationRepository(
    private val context: Context
) {

    private var currentData: DonationDetail? = null

    fun observerDonationDetail(): Observable<DonationDetail> = Observable
        .fromCallable { getAssetsData() }

    fun getAssetsData(): DonationDetail {
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