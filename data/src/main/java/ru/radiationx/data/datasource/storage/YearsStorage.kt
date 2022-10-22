package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.entity.app.release.YearItem
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

    private val localYears = mutableListOf<YearItem>()
    private val localYearsRelay = MutableStateFlow(localYears.toList())

    init {
        loadAll()
    }

    override fun observeYears(): Flow<List<YearItem>> = localYearsRelay

    override fun saveYears(years: List<YearItem>) {
        localYears.clear()
        localYears.addAll(years)
        saveAll()
        localYearsRelay.value = localYears.toList()
    }

    override fun getYears(): List<YearItem> = localYears

    private fun saveAll() {
        val jsonYears = JSONArray()
        localYears.forEach {
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

    private fun loadAll() {
        val savedYears = sharedPreferences.getString(LOCAL_YEARS_KEY, null)
        savedYears?.let {
            val jsonYears = JSONArray(it)
            (0 until jsonYears.length()).forEach { index ->
                jsonYears.getJSONObject(index).let {
                    localYears.add(YearItem().apply {
                        title = it.getString("title")
                        value = it.getString("value")
                    })
                }
            }
        }
        localYearsRelay.value = localYears.toList()
    }
}