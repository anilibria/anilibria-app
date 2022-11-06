package ru.radiationx.data.entity.domain.search

import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem

data class SearchForm(
    val years: List<YearItem>? = null,
    val seasons: List<SeasonItem>? = null,
    val genres: List<GenreItem>? = null,
    val sort: Sort = Sort.RATING,
    val onlyCompleted: Boolean = false
) {
    enum class Sort {
        RATING, DATE
    }
}