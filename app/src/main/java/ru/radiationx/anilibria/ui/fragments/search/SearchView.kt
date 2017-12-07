package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.data.api.GenreItem
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesView

interface SearchView : ReleasesView {
    fun showGenres(genres: List<GenreItem>)
}
