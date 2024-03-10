package ru.radiationx.data.ads

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.ads.remote.AdsConfigResponse
import timber.log.Timber
import javax.inject.Inject

class AdsConfigStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi,
) {

    companion object {
        private const val KEY_ADS_CONFIG = "data.adsconfig_v2"
    }

    private val adapter by lazy { moshi.adapter(AdsConfigResponse::class.java) }

    suspend fun save(config: AdsConfigResponse) {
        withContext(Dispatchers.IO) {
            try {
                val json = adapter.toJson(config)
                sharedPreferences.edit().putString(KEY_ADS_CONFIG, json.toString()).apply()
            } catch (ex: Throwable) {
                Timber.e(ex)
            }
        }
    }

    suspend fun get(): AdsConfigResponse? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(KEY_ADS_CONFIG, null)
                ?.let { adapter.fromJson(it) }
        }
    }
}