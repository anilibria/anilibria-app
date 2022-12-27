package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.GenreItem

interface GenresHolder {
    fun observeGenres(): Flow<List<GenreItem>>
    suspend fun saveGenres(genres: List<GenreItem>)
    suspend fun getGenres(): List<GenreItem>
}