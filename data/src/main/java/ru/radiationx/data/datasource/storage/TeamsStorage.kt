package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onSubscription
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.TeamsHolder
import ru.radiationx.data.entity.app.team.TeamsResponse
import toothpick.InjectConstructor

@InjectConstructor
class TeamsStorage(
    private val moshi: Moshi,
    @DataPreferences private val sharedPreferences: SharedPreferences
) : TeamsHolder {

    companion object {
        private const val KEY_DONATION = "teams"
    }

    private val dataAdapter by lazy {
        moshi.adapter(TeamsResponse::class.java)
    }

    private val dataRelay = MutableStateFlow<TeamsResponse?>(null)

    override fun observe(): Flow<TeamsResponse> = dataRelay
        .onSubscription {
            if (dataRelay.value == null) {
                updateCurrentData()
            }
        }
        .filterNotNull()

    override suspend fun get(): TeamsResponse {
        if (dataRelay.value == null) {
            updateCurrentData()
        }
        return requireNotNull(dataRelay.value)
    }

    override suspend fun save(data: TeamsResponse) {
        saveToPrefs(data)
        updateCurrentData()
    }

    override suspend fun delete() {
        deleteFromPrefs()
        updateCurrentData()
    }

    private fun updateCurrentData() {
        val prefsData = try {
            getFromPrefs()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
        dataRelay.value = prefsData
    }

    private fun deleteFromPrefs() {
        sharedPreferences.edit().remove(KEY_DONATION).apply()
    }

    private fun saveToPrefs(data: TeamsResponse) {
        val json = dataAdapter.toJson(data)
        sharedPreferences.edit()
            .putString(KEY_DONATION, json)
            .apply()
    }

    private fun getFromPrefs(): TeamsResponse? = sharedPreferences
        .getString(KEY_DONATION, null)
        ?.let { dataAdapter.fromJson(it) }
}