package ru.radiationx.data.app.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import ru.radiationx.data.app.config.db.AppConfigDataDb
import ru.radiationx.data.app.config.mapper.toDb
import ru.radiationx.data.app.config.mapper.toDomain
import ru.radiationx.data.app.config.models.AppConfigData
import ru.radiationx.data.app.config.remote.AppConfigResponse
import ru.radiationx.data.di.DataPreferences
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AppConfigStorage @Inject constructor(
    private val context: Context,
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) {

    companion object {
        private const val KEY_CONFIG = "data.app_config_v3"
    }

    private val dataAdapter by lazy {
        moshi.adapter(AppConfigDataDb::class.java)
    }

    private val assetAdapter by lazy {
        moshi.adapter(AppConfigResponse::class.java)
    }

    private val dataFlow = SuspendMutableStateFlow {
        loadConfig()
    }

    suspend fun get(): AppConfigData {
        return dataFlow.getValue()
    }

    suspend fun save(config: AppConfigData) {
        saveConfig(config)
        dataFlow.setValue(loadConfig())
    }

    private suspend fun saveConfig(config: AppConfigData) {
        withContext(Dispatchers.IO) {
            coRunCatching {
                val dbConfig = config.toDb()
                val json = dataAdapter.toJson(dbConfig)
                sharedPreferences.edit { putString(KEY_CONFIG, json.toString()) }
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private suspend fun loadConfig(): AppConfigData {
        return withContext(Dispatchers.IO) {
            val resultPrefs = coRunCatching {
                sharedPreferences
                    .getString(KEY_CONFIG, null)
                    ?.let { dataAdapter.fromJson(it) }
                    ?.toDomain()
            }.getOrNull()
            resultPrefs ?: getFromAssets()
        }
    }

    private fun getFromAssets(): AppConfigData {
        return context.assets.open("config-v2.json")
            .use { stream ->
                stream.source().buffer().use { reader ->
                    requireNotNull(assetAdapter.fromJson(reader))
                }
            }
            .toDomain()
    }
}