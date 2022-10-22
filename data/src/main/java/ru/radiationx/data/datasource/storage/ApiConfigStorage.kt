package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.parsers.ConfigurationParser
import javax.inject.Inject

class ApiConfigStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val configurationParser: ConfigurationParser
) {

    companion object {
        private const val KEY_API_CONFIG = "data.apiconfig_v2"
        private const val KEY_API_CONFIG_ACTIVE = "data.apiconfig_active_v2"
    }

    fun saveJson(json: JSONObject) {
        try {
            sharedPreferences.edit().putString(KEY_API_CONFIG, json.toString()).apply()
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    fun get(): List<ApiAddress>? = sharedPreferences
        .getString(KEY_API_CONFIG, null)
        ?.let { configurationParser.parse(JSONObject(it)) }

    fun setActive(tag: String) =
        sharedPreferences.edit().putString(KEY_API_CONFIG_ACTIVE, tag).apply()

    fun getActive(): String? = sharedPreferences
        .getString(KEY_API_CONFIG_ACTIVE, null)
}