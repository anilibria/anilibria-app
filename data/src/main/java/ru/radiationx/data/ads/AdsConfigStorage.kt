package ru.radiationx.data.ads

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
        private const val KEY_ADS_CONFIG = "data.adsconfig"
    }

    private val adapter by lazy {
        val type = Types.newParameterizedType(List::class.java, AdsConfigResponse::class.java)
        moshi.adapter<List<AdsConfigResponse>>(type)
    }

    suspend fun save(config: List<AdsConfigResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val json = adapter.toJson(config)
                sharedPreferences.edit().putString(KEY_ADS_CONFIG, json.toString()).apply()
            } catch (ex: Throwable) {
                Timber.e(ex)
            }
        }
    }

    suspend fun get(): List<AdsConfigResponse>? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(KEY_ADS_CONFIG, null)
                ?.let { adapter.fromJson(it) }
        }
    }
}