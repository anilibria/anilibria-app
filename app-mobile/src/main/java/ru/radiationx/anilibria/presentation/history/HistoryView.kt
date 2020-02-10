package ru.radiationx.anilibria.presentation.history

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.release.ReleaseItem

/**
 * Created by radiationx on 18.02.18.
 */
interface HistoryView : IBaseView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showReleases(releases: List<ReleaseItem>)
}