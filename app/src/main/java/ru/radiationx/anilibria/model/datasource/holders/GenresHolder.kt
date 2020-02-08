package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable
import ru.radiationx.data.entity.app.release.GenreItem

interface GenresHolder {
    fun observeGenres(): Observable<MutableList<GenreItem>>
    fun saveGenres(genres: List<GenreItem>)
    fun getGenres(): List<GenreItem>
}