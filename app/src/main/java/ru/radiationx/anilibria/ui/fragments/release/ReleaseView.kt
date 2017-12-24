package ru.radiationx.anilibria.ui.fragments.release

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/* Created by radiationx on 18.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseView : IBaseView {
    fun showRelease(release: ReleaseItem)

    @StateStrategyType(SkipStrategy::class)
    fun loadTorrent(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun shareRelease(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun copyLink(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisodes(release: ReleaseItem)

    @StateStrategyType(SkipStrategy::class)
    fun playMoonwalk(link: String)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisode(release: ReleaseItem, position: Int, quality: Int)
}
