package ru.radiationx.anilibria.presentation.search

import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.presentation.release.list.ReleasesView

interface SearchView : ReleasesView {
    fun showGenres(genres: List<GenreItem>)
    fun showFastItems(items: List<SearchItem>)
}
