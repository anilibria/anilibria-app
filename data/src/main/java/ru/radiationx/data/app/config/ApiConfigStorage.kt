package ru.radiationx.data.app.config

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.app.config.remote.ApiConfigResponse
import ru.radiationx.data.di.DataPreferences
import timber.log.Timber
import javax.inject.Inject

class ApiConfigStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) {

    companion object {
        private const val KEY_API_CONFIG = "data.apiconfig_v2"
        private const val KEY_API_CONFIG_ACTIVE = "data.apiconfig_active_v2"
    }

    private val adapter by lazy {
        moshi.adapter(ApiConfigResponse::class.java)
    }

    suspend fun save(config: ApiConfigResponse) {
        withContext(Dispatchers.IO) {
            try {
                val json = adapter.toJson(config)
                sharedPreferences.edit().putString(KEY_API_CONFIG, json.toString()).apply()
            } catch (ex: Throwable) {
                Timber.e(ex)
            }
        }
    }

    suspend fun get(): ApiConfigResponse? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(KEY_API_CONFIG, null)
                ?.let { adapter.fromJson(it) }
        }
    }

    suspend fun setActive(tag: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_API_CONFIG_ACTIVE, tag).apply()
        }
    }

    suspend fun getActive(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(KEY_API_CONFIG_ACTIVE, null)
        }
    }
}