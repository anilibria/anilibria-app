package ru.radiationx.anilibria.presentation.search

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.YearItem
import ru.radiationx.anilibria.presentation.release.list.ReleasesView

@StateStrategyType(AddToEndSingleStrategy::class)
interface SearchView : ReleasesView {
    fun updateInfo(sort: String, filters: Int)

    fun showGenres(genres: List<GenreItem>)
    fun showYears(years: List<YearItem>)
    fun selectGenres(genres: List<String>)
    fun selectYears(years: List<String>)
    fun setSorting(sorting: String)

    @StateStrategyType(SkipStrategy::class)
    fun showDialog()
}
