package ru.radiationx.anilibria.screen.search

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SearchForm
import toothpick.InjectConstructor

@InjectConstructor
class SearchController {

    val yearsEvent = MutableSharedFlow<List<YearItem>>()
    val seasonsEvent = MutableSharedFlow<List<SeasonItem>>()
    val genresEvent = MutableSharedFlow<List<GenreItem>>()
    val sortEvent = MutableSharedFlow<SearchForm.Sort>()
    val completedEvent = MutableSharedFlow<Boolean>()

    val applyFormEvent = MutableSharedFlow<SearchForm>()
}