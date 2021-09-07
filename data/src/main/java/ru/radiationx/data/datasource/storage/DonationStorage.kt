package ru.radiationx.data.datasource.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.entity.app.donation.DonationDetail
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

    private val dataRelay = BehaviorRelay.create<DonationDetail>()

    override fun observe(): Observable<DonationDetail> = dataRelay
        .hide()
        .doOnSubscribe {
            if (!dataRelay.hasValue()) {
                updateCurrentData()
            }
        }

    override fun get(): Single<DonationDetail> = Single
        .fromCallable {
            if (!dataRelay.hasValue()) {
                updateCurrentData()
            }
            requireNotNull(dataRelay.value)
        }

    override fun save(data: DonationDetail): Completable = Completable
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

    private fun saveToPrefs(data: DonationDetail) {
        val json = gson.toJson(data, DonationDetail::class.java)
        sharedPreferences.edit()
            .putString(KEY_DONATION, json)
            .apply()
    }

    private fun getFromPrefs(): DonationDetail? = sharedPreferences
        .getString(KEY_DONATION, null)
        ?.let { gson.fromJson(it, DonationDetail::class.java) }

    private fun getFromAssets(): DonationDetail =
        context.assets.open("donation_detail_info.json").use { stream ->
            stream.bufferedReader().use { reader ->
                gson.fromJson(reader, DonationDetail::class.java)
            }
        }
}