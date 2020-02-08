package ru.radiationx.anilibria.presentation.search

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.anilibria.presentation.release.list.ReleasesView

@StateStrategyType(AddToEndSingleStrategy::class)
interface SearchView : ReleasesView {
    fun updateInfo(sort: String, filters: Int)

    fun showGenres(genres: List<GenreItem>)
    fun showYears(years: List<YearItem>)
    fun showSeasons(seasons: List<SeasonItem>)
    fun selectGenres(genres: List<String>)
    fun selectYears(years: List<String>)
    fun selectSeasons(seasons: List<String>)
    fun setSorting(sorting: String)
    fun setComplete(complete: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun showDialog()
}
