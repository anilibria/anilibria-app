package ru.radiationx.anilibria.screen.search

import ru.radiationx.data.api.shared.filter.legacy.GenreItem
import ru.radiationx.data.api.shared.filter.legacy.SearchForm
import ru.radiationx.data.api.shared.filter.legacy.SeasonItem
import ru.radiationx.data.api.shared.filter.legacy.YearItem
import ru.radiationx.shared.ktx.EventFlow
import javax.inject.Inject

class SearchController @Inject constructor() {

    val yearsEvent = EventFlow<Set<YearItem>>()
    val seasonsEvent = EventFlow<Set<SeasonItem>>()
    val genresEvent = EventFlow<Set<GenreItem>>()
    val sortEvent = EventFlow<SearchForm.Sort>()
    val completedEvent = EventFlow<Boolean>()

    val applyFormEvent = EventFlow<SearchForm>()
}