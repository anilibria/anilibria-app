package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.entity.domain.release.YearItem
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class YearsStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : YearsHolder {

    companion object {
        private const val LOCAL_YEARS_KEY = "data.local_years"
    }

    private val localYearsRelay = SuspendMutableStateFlow {
        loadAll()
    }

    override fun observeYears(): Flow<List<YearItem>> = localYearsRelay

    override suspend fun saveYears(years: List<YearItem>) {
        localYearsRelay.setValue(years)
        saveAll()
    }

    override suspend fun getYears(): List<YearItem> = localYearsRelay.getValue()

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonYears = JSONArray()
            localYearsRelay.getValue().forEach {
                jsonYears.put(JSONObject().apply {
                    put("title", it.title)
                    put("value", it.value)
                })
            }
            sharedPreferences
                .edit()
                .putString(LOCAL_YEARS_KEY, jsonYears.toString())
                .apply()
        }
    }

    private suspend fun loadAll(): List<YearItem> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<YearItem>()
            val savedYears = sharedPreferences.getString(LOCAL_YEARS_KEY, null)
            savedYears?.let {
                val jsonYears = JSONArray(it)
                (0 until jsonYears.length()).forEach { index ->
                    jsonYears.getJSONObject(index).let {
                        result.add(
                            YearItem(
                                title = it.getString("title"),
                                value = it.getString("value")
                            )
                        )
                    }
                }
            }
            result
        }
    }
}