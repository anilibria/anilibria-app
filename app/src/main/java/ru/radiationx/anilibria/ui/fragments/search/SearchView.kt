package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.data.api.models.GenreItem
import ru.radiationx.anilibria.data.api.models.SearchItem
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesView

interface SearchView : ReleasesView {
    fun showGenres(genres: List<GenreItem>)
    fun showFastItems(items: List<SearchItem>)
}
