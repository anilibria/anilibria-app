package ru.radiationx.anilibria.ui.fragments.release

import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

import ru.radiationx.anilibria.data.api.releases.ReleaseItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/* Created by radiationx on 18.11.17. */

@StateStrategyType(AddToEndStrategy::class)
interface ReleaseView : IBaseView {
    fun showRelease(release: ReleaseItem)

    @StateStrategyType(SkipStrategy::class)
    fun loadTorrent(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun shareRelease(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun copyLink(url: String)
}
