package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.GenreItem

interface GenresHolder {
    fun observeGenres(): Flow<List<GenreItem>>
    fun saveGenres(genres: List<GenreItem>)
    fun getGenres(): List<GenreItem>
}