package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.entity.app.release.GenreItem
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

    private val localGenres by lazy {
        loadAll().toMutableList()
    }
    private val localGenresRelay by lazy {
        MutableStateFlow(localGenres.toList())
    }

    override fun observeGenres(): Flow<List<GenreItem>> = localGenresRelay

    override fun saveGenres(genres: List<GenreItem>) {
        localGenres.clear()
        localGenres.addAll(genres)
        saveAll()
        localGenresRelay.value = localGenres.toList()
    }

    override fun getGenres(): List<GenreItem> = localGenres

    private fun saveAll() {
        val jsonGenres = JSONArray()
        localGenres.forEach {
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

    private fun loadAll(): List<GenreItem> {
        val result = mutableListOf<GenreItem>()
        val savedGenres = sharedPreferences.getString(LOCAL_GENRES_KEY, null)
        savedGenres?.let {
            val jsonGenres = JSONArray(it)
            (0 until jsonGenres.length()).forEach {
                jsonGenres.getJSONObject(it).let {
                    result.add(GenreItem().apply {
                        title = it.getString("title")
                        value = it.getString("value")
                    })
                }
            }
        }
        return result
    }
}