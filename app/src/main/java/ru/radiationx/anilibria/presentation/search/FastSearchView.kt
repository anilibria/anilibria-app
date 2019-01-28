package ru.radiationx.anilibria.presentation.search

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.search.FastSearchItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface FastSearchView : IBaseView {
    fun showSearchItems(items: List<FastSearchItem>, query: String)
    fun setSearchProgress(isProgress: Boolean)
}