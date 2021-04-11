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

    fun observerDonationDetail(): Observable<DonationDetail> = Observable
        .fromCallable { getAssetsData() }

    fun getAssetsData(): DonationDetail {
        val gson = Gson()
        val data = context.assets.open("donation_detail_info.json").use { stream ->
            stream.bufferedReader().use { reader ->
                gson.fromJson<DonationDetail>(reader, DonationDetail::class.java)
            }
        }
        return data
    }
}