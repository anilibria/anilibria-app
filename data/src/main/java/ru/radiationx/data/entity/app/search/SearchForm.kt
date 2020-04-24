package ru.radiationx.data.entity.app.search

import ru.radiationx.data.datasource.storage.GenresStorage
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.SeasonItem

data class SearchForm(
    val years: List<String>? = null,
    val seasons: List<SeasonItem>? = null,
    val genres: List<GenreItem>? = null,
    val sort: Sort = Sort.RATING,
    val onlyCompleted: Boolean = false
) {
    enum class Sort {
        RATING, DATE
    }
}