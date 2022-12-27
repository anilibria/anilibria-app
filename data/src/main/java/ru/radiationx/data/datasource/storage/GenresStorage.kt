package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.entity.domain.release.GenreItem
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class GenresStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : GenresHolder {

    companion object {
        private const val LOCAL_GENRES_KEY = "data.local_genres"
    }

    private val localGenresRelay = SuspendMutableStateFlow {
        loadAll()
    }

    override fun observeGenres(): Flow<List<GenreItem>> = localGenresRelay

    override suspend fun saveGenres(genres: List<GenreItem>) {
        localGenresRelay.setValue(genres.toList())
        saveAll()
    }

    override suspend fun getGenres(): List<GenreItem> = localGenresRelay.getValue()

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonGenres = JSONArray()
            localGenresRelay.getValue().forEach {
                jsonGenres.put(JSONObject().apply {
                    put("title", it.title)
                    put("value", it.value)
                })
            }
            sharedPreferences
                .edit()
                .putString(LOCAL_GENRES_KEY, jsonGenres.toString())
                .apply()
        }
    }

    private suspend fun loadAll(): List<GenreItem> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<GenreItem>()
            val savedGenres = sharedPreferences.getString(LOCAL_GENRES_KEY, null)
            savedGenres?.let {
                val jsonGenres = JSONArray(it)
                (0 until jsonGenres.length()).forEach {
                    jsonGenres.getJSONObject(it).let {
                        result.add(
                            GenreItem(
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