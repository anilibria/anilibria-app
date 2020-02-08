package ru.radiationx.anilibria.presentation.release.details

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.common.IBaseView

/* Created by radiationx on 18.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseView : IBaseView {

    fun showRelease(release: ReleaseFull)

    @StateStrategyType(SkipStrategy::class)
    fun shareRelease(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun copyLink(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun addShortCut(release: ReleaseItem)
}
