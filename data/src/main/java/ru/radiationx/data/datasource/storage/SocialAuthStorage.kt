package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.SocialAuthHolder
import ru.radiationx.data.entity.app.auth.SocialAuth
import javax.inject.Inject

class SocialAuthStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : SocialAuthHolder {

    private val dataRelay by lazy { MutableStateFlow(getSavedData()) }

    override fun get(): List<SocialAuth> = dataRelay.value

    override fun observe(): Flow<List<SocialAuth>> = dataRelay

    override fun save(items: List<SocialAuth>) {
        saveData(items)
        dataRelay.value = items.toList()
    }

    override fun delete() {
        sharedPreferences.edit().remove("social_auth").apply()
        dataRelay.value = emptyList()
    }

    private fun getSavedData(): List<SocialAuth> {
        val resultItems = mutableListOf<SocialAuth>()
        sharedPreferences.getString("social_auth", null)?.also {
            val itemsJson = JSONArray(it)
            for (j in 0 until itemsJson.length()) {
                val jsonItem = itemsJson.getJSONObject(j)
                resultItems.add(
                    SocialAuth(
                        jsonItem.getString("key"),
                        jsonItem.getString("title"),
                        jsonItem.getString("socialUrl"),
                        jsonItem.getString("resultPattern"),
                        jsonItem.getString("errorUrlPattern")
                    )
                )
            }
        }
        return resultItems
    }

    private fun saveData(items: List<SocialAuth>) {
        val resultJson = JSONArray()
        items.forEach { item ->
            resultJson.put(JSONObject().apply {
                put("key", item.key)
                put("title", item.title)
                put("socialUrl", item.socialUrl)
                put("resultPattern", item.resultPattern)
                put("errorUrlPattern", item.errorUrlPattern)
            })
        }
        sharedPreferences.edit().putString("social_auth", resultJson.toString()).apply()
    }

}