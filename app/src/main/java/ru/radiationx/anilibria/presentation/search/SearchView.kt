package ru.radiationx.anilibria.presentation.search

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.presentation.release.list.ReleasesView

@StateStrategyType(AddToEndSingleStrategy::class)
interface SearchView : ReleasesView {
    fun showGenres(genres: List<GenreItem>)
    fun selectGenres(genres: List<String>)

    @StateStrategyType(SkipStrategy::class)
    fun showDialog()

    fun showFastItems(items: List<SearchItem>)
}
