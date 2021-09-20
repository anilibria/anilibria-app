package ru.radiationx.data.datasource.storage

import android.content.Context
import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okio.buffer
import okio.source
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.entity.app.donation.DonationInfoResponse
import toothpick.InjectConstructor

@InjectConstructor
class DonationStorage(
    private val moshi: Moshi,
    private val context: Context,
    @DataPreferences private val sharedPreferences: SharedPreferences
) : DonationHolder {

    companion object {
        private const val KEY_DONATION = "donation_detail"
    }

    private val dataAdapter by lazy {
        moshi.adapter(DonationInfoResponse::class.java)
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
        val json = dataAdapter.toJson(data)
        sharedPreferences.edit()
            .putString(KEY_DONATION, json)
            .apply()
    }

    private fun getFromPrefs(): DonationInfoResponse? = sharedPreferences
        .getString(KEY_DONATION, null)
        ?.let { dataAdapter.fromJson(it) }

    private fun getFromAssets(): DonationInfoResponse =
        context.assets.open("donation_info.json").use { stream ->
            stream.source().buffer().use { reader ->
                requireNotNull(dataAdapter.fromJson(reader))
            }
        }
}