package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.entity.response.config.ApiConfigResponse
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

    fun save(config: ApiConfigResponse) {
        try {
            val json = adapter.toJson(config)
            sharedPreferences.edit().putString(KEY_API_CONFIG, json.toString()).apply()
        } catch (ex: Throwable) {
            Timber.e(ex)
        }
    }

    fun get(): ApiConfigResponse? = sharedPreferences
        .getString(KEY_API_CONFIG, null)
        ?.let { adapter.fromJson(it) }

    fun setActive(tag: String) =
        sharedPreferences.edit().putString(KEY_API_CONFIG_ACTIVE, tag).apply()

    fun getActive(): String? = sharedPreferences
        .getString(KEY_API_CONFIG_ACTIVE, null)
}