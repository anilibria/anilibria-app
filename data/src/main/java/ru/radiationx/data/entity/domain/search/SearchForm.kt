package ru.radiationx.data.entity.domain.search

import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem

data class SearchForm(
    val years: Set<YearItem> = emptySet(),
    val seasons: Set<SeasonItem> = emptySet(),
    val genres: Set<GenreItem> = emptySet(),
    val sort: Sort = Sort.RATING,
    val onlyCompleted: Boolean = false
) {
    enum class Sort {
        RATING, DATE
    }
}