package ru.radiationx.anilibria.presentation.youtube

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.youtube.YoutubeItem

@StateStrategyType(AddToEndSingleStrategy::class)
interface YoutubeView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showItems(items: List<YoutubeItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(items: List<YoutubeItem>)

    fun setEndless(enable: Boolean)
}