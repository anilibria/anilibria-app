package ru.radiationx.anilibria.presentation.search

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface FastSearchView : IBaseView {
    fun showSearchItems(items: List<SearchItem>)
    fun setSearchProgress(isProgress: Boolean)
}