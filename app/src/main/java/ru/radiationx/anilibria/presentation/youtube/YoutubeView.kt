package ru.radiationx.anilibria.presentation.youtube

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface YoutubeView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showItems(items: List<YoutubeItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(items: List<YoutubeItem>)

    fun setEndless(enable: Boolean)
}