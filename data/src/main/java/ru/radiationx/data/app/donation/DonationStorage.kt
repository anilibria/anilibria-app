package ru.radiationx.data.app.donation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import ru.radiationx.data.app.donation.remote.DonationInfoResponse
import ru.radiationx.data.di.DataPreferences
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import timber.log.Timber
import javax.inject.Inject

class DonationStorage @Inject constructor(
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

    private val dataRelay = SuspendMutableStateFlow {
        loadData()
    }

    override fun observe(): Flow<DonationInfoResponse> {
        return dataRelay.filterNotNull()
    }

    override suspend fun get(): DonationInfoResponse {
        return dataRelay.getValue()
    }

    override suspend fun save(data: DonationInfoResponse) {
        saveData(data)
        dataRelay.setValue(loadData())
    }

    private suspend fun loadData(): DonationInfoResponse {
        return withContext(Dispatchers.IO) {
            val prefsData = try {
                getFromPrefs()
            } catch (ex: Exception) {
                Timber.e(ex)
                null
            }
            prefsData ?: getFromAssets()
        }
    }

    private suspend fun saveData(data: DonationInfoResponse) {
        withContext(Dispatchers.IO) {
            val json = dataAdapter.toJson(data)
            sharedPreferences.edit {
                putString(KEY_DONATION, json)
            }
        }
    }

    private fun getFromPrefs(): DonationInfoResponse? = sharedPreferences
        .getString(KEY_DONATION, null)
        ?.let { dataAdapter.fromJson(it) }

    private fun getFromAssets(): DonationInfoResponse =
        context.assets.open("donations-config.json").use { stream ->
            stream.source().buffer().use { reader ->
                requireNotNull(dataAdapter.fromJson(reader))
            }
        }
}