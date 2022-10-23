package ru.radiationx.data.datasource.storage

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onSubscription
import okio.buffer
import okio.source
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.entity.app.donation.DonationInfoResponse
import timber.log.Timber
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

    private val dataRelay = MutableStateFlow<DonationInfoResponse?>(null)

    override fun observe(): Flow<DonationInfoResponse> {
        return dataRelay
            .onSubscription {
                if (dataRelay.value == null) {
                    updateCurrentData()
                }
            }
            .filterNotNull()
    }

    override suspend fun get(): DonationInfoResponse {
        if (dataRelay.value == null) {
            updateCurrentData()
        }
        return requireNotNull(dataRelay.value)
    }

    override suspend fun save(data: DonationInfoResponse) {

        saveToPrefs(data)
        updateCurrentData()
    }

    override suspend fun delete() {
        deleteFromPrefs()
        updateCurrentData()
    }

    private fun updateCurrentData() {
        val prefsData = try {
            getFromPrefs()
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
        val data = prefsData ?: getFromAssets()
        dataRelay.value = data
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