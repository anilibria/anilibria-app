package ru.radiationx.data.api.shared.filter.legacy

@Deprecated("")
data class SearchForm(
    val years: Set<YearItem> = emptySet(),
    val seasons: Set<SeasonItem> = emptySet(),
    val genres: Set<GenreItem> = emptySet(),
    val sort: Sort = Sort.RATING,
    val onlyCompleted: Boolean = false
) {
    @Deprecated("")
    enum class Sort {
        RATING, DATE
    }
}