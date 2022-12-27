package ru.radiationx.anilibria.screen.search

import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.shared.ktx.EventFlow
import toothpick.InjectConstructor

@InjectConstructor
class SearchController {

    val yearsEvent = EventFlow<Set<YearItem>>()
    val seasonsEvent = EventFlow<Set<SeasonItem>>()
    val genresEvent = EventFlow<Set<GenreItem>>()
    val sortEvent = EventFlow<SearchForm.Sort>()
    val completedEvent = EventFlow<Boolean>()

    val applyFormEvent = EventFlow<SearchForm>()
}