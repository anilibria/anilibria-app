package ru.radiationx.anilibria.presentation.history

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 18.02.18.
 */
interface HistoryView : IBaseView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showReleases(releases: List<ReleaseItem>)
}