package ru.radiationx.data.app.history

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.radiationx.data.app.history.db.ReleaseHistoryDb
import ru.radiationx.data.app.history.mapper.toDomain
import ru.radiationx.data.app.history.mapper.toHistoryDb
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.di.DataPreferences
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) : HistoryHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.local_history_new"
    }

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, ReleaseHistoryDb::class.java)
        moshi.adapter<List<ReleaseHistoryDb>>(type)
    }

    private val dataFlow = SuspendMutableStateFlow {
        loadAll()
    }

    override suspend fun getIds(): List<ReleaseId> {
        return dataFlow.getValue()
    }

    override fun observeIds(): Flow<List<ReleaseId>> {
        return dataFlow
    }

    override suspend fun putId(id: ReleaseId) {
        dataFlow.update { data ->
            val mutableData = data.toMutableList()
            mutableData
                .firstOrNull { it == id }
                ?.let { mutableData.remove(it) }
            mutableData.add(id)
            mutableData
        }
        saveAll()
    }

    override suspend fun putAllIds(ids: List<ReleaseId>) {
        dataFlow.update { data ->
            val mutableData = data.toMutableList()
            ids.forEach { id ->
                mutableData
                    .firstOrNull { it == id }
                    ?.let { mutableData.remove(it) }
                mutableData.add(id)
            }
            mutableData
        }
        saveAll()
    }

    override suspend fun removeId(id: ReleaseId) {
        dataFlow.update { data ->
            val mutableData = data.toMutableList()
            mutableData.firstOrNull { it == id }?.also {
                mutableData.remove(it)
            }
            mutableData
        }
        saveAll()
    }

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val dbItems = dataFlow.getValue().map { it.toHistoryDb() }
            val json = dataAdapter.toJson(dbItems)
            sharedPreferences.edit {
                putString(LOCAL_HISTORY_KEY, json)
            }
        }
    }

    private suspend fun loadAll(): List<ReleaseId> {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(LOCAL_HISTORY_KEY, null)
                ?.let { dataAdapter.fromJson(it) }
                ?.map { it.toDomain() }
                .orEmpty()
        }
    }
}