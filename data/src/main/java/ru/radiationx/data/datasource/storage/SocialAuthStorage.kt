package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.SocialAuthHolder
import ru.radiationx.data.entity.domain.auth.SocialAuth
import javax.inject.Inject

class SocialAuthStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : SocialAuthHolder {

    private val dataRelay = SuspendMutableStateFlow {
        getSavedData()
    }

    override suspend fun get(): List<SocialAuth> = dataRelay.getValue()

    override fun observe(): Flow<List<SocialAuth>> = dataRelay

    override suspend fun save(items: List<SocialAuth>) {
        saveData(items)
        dataRelay.setValue(getSavedData())
    }

    override suspend fun delete() {
        saveData(emptyList())
        dataRelay.setValue(getSavedData())
    }

    private suspend fun getSavedData(): List<SocialAuth> {
        return withContext(Dispatchers.IO) {
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
            resultItems
        }
    }

    private suspend fun saveData(items: List<SocialAuth>) {
        withContext(Dispatchers.IO) {
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

}