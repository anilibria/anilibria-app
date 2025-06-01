package ru.radiationx.data.datasource.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.MenuHolder
import ru.radiationx.data.entity.db.LinkMenuDb
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.mapper.toDb
import ru.radiationx.data.entity.mapper.toDomain
import javax.inject.Inject

class MenuStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi,
    private val context: Context
) : MenuHolder {

    companion object {
        private const val LOCAL_MENU_KEY = "data.local_menu"
    }

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, LinkMenuDb::class.java)
        moshi.adapter<List<LinkMenuDb>>(type)
    }

    private val dataFlow = SuspendMutableStateFlow {
        loadData()
    }

    override fun observe(): Flow<List<LinkMenuItem>> {
        return dataFlow
    }

    override suspend fun get(): List<LinkMenuItem> {
        return dataFlow.getValue()
    }

    override suspend fun save(items: List<LinkMenuItem>) {
        saveData(items)
        dataFlow.setValue(loadData())
    }

    private suspend fun saveData(items: List<LinkMenuItem>) {
        withContext(Dispatchers.IO) {
            val dbItems = items.map { it.toDb() }
            val json = dataAdapter.toJson(dbItems)
            sharedPreferences.edit {
                putString(LOCAL_MENU_KEY, json)
            }
        }
    }

    private suspend fun loadData(): List<LinkMenuItem> {
        return withContext(Dispatchers.IO) {
            val items = sharedPreferences.getString(LOCAL_MENU_KEY, null)
                ?.let { dataAdapter.fromJson(it) }
                ?: getFromAssets()
            items.map { it.toDomain() }
        }
    }

    private fun getFromAssets(): List<LinkMenuDb> {
        return context.assets.open("menu-config.json").use { stream ->
            stream.source().buffer().use { reader ->
                requireNotNull(dataAdapter.fromJson(reader))
            }
        }
    }
}