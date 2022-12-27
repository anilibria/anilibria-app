package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.TeamsHolder
import ru.radiationx.data.entity.response.team.TeamsResponse
import timber.log.Timber
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

    private val dataRelay by lazy {
        MutableStateFlow(getCurrentData())
    }

    override fun observe(): Flow<TeamsResponse> = dataRelay
        .filterNotNull()

    override suspend fun get(): TeamsResponse {
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
        dataRelay.value = getCurrentData()
    }

    private fun getCurrentData(): TeamsResponse? {
        return try {
            getFromPrefs()
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
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