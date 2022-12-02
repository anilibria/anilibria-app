package ru.radiationx.anilibria.screen.search

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SearchForm
import toothpick.InjectConstructor

@InjectConstructor
class SearchController {

    val yearsEvent = MutableSharedFlow<Set<YearItem>>()
    val seasonsEvent = MutableSharedFlow<Set<SeasonItem>>()
    val genresEvent = MutableSharedFlow<Set<GenreItem>>()
    val sortEvent = MutableSharedFlow<SearchForm.Sort>()
    val completedEvent = MutableSharedFlow<Boolean>()

    val applyFormEvent = MutableSharedFlow<SearchForm>()
}