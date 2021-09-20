package ru.radiationx.data.datasource.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.entity.app.donation.DonationDetailResponse
import ru.radiationx.data.entity.app.donation.DonationInfoResponse
import toothpick.InjectConstructor
import java.lang.Exception

@InjectConstructor
class DonationStorage(
    private val gson: Gson,
    private val context: Context,
    @DataPreferences private val sharedPreferences: SharedPreferences
) : DonationHolder {

    companion object {
        private const val KEY_DONATION = "donation_detail"
    }

    private val dataRelay = BehaviorRelay.create<DonationInfoResponse>()

    override fun observe(): Observable<DonationInfoResponse> = dataRelay
        .hide()
        .doOnSubscribe {
            if (!dataRelay.hasValue()) {
                updateCurrentData()
            }
        }

    override fun get(): Single<DonationInfoResponse> = Single
        .fromCallable {
            if (!dataRelay.hasValue()) {
                updateCurrentData()
            }
            requireNotNull(dataRelay.value)
        }

    override fun save(data: DonationInfoResponse): Completable = Completable
        .fromAction {
            saveToPrefs(data)
            updateCurrentData()
        }

    override fun delete(): Completable = Completable
        .fromAction {
            deleteFromPrefs()
            updateCurrentData()
        }

    private fun updateCurrentData() {
        val prefsData = try {
            getFromPrefs()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
        val data = prefsData ?: getFromAssets()
        dataRelay.accept(data)
    }

    private fun deleteFromPrefs() {
        sharedPreferences.edit().remove(KEY_DONATION).apply()
    }

    private fun saveToPrefs(data: DonationInfoResponse) {
        val json = gson.toJson(data, DonationInfoResponse::class.java)
        sharedPreferences.edit()
            .putString(KEY_DONATION, json)
            .apply()
    }

    private fun getFromPrefs(): DonationInfoResponse? = sharedPreferences
        .getString(KEY_DONATION, null)
        ?.let { gson.fromJson(it, DonationInfoResponse::class.java) }

    private fun getFromAssets(): DonationInfoResponse =
        context.assets.open("donation_info.json").use { stream ->
            stream.bufferedReader().use { reader ->
                gson.fromJson(reader, DonationInfoResponse::class.java)
            }
        }
}