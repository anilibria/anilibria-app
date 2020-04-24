package ru.radiationx.anilibria.screen.search

import com.jakewharton.rxrelay2.PublishRelay
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.entity.app.search.SearchForm
import toothpick.InjectConstructor

@InjectConstructor
class SearchController {

    val yearsEvent = PublishRelay.create<List<String>>()
    val seasonsEvent = PublishRelay.create<List<SeasonItem>>()
    val genresEvent = PublishRelay.create<List<GenreItem>>()
    val sortEvent = PublishRelay.create<SearchForm.Sort>()
    val completedEvent = PublishRelay.create<Boolean>()

    val applyFormEvent = PublishRelay.create<SearchForm>()
}