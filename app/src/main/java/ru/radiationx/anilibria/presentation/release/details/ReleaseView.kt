package ru.radiationx.anilibria.presentation.release.details

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.TorrentItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/* Created by radiationx on 18.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseView : IBaseView {
    fun showVitalItems(vital: List<VitalItem>)

    fun updateFavCounter()

    fun showRelease(release: ReleaseFull)

    @StateStrategyType(AddToEndStrategy::class)
    fun showComments(comments: List<Comment>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMoreComments(comments: List<Comment>)

    fun setEndlessComments(enable: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun loadTorrent(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showTorrentDialog(torrent: List<TorrentItem>)

    @StateStrategyType(SkipStrategy::class)
    fun shareRelease(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun copyLink(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisodes(release: ReleaseFull)

    @StateStrategyType(SkipStrategy::class)
    fun playContinue(release: ReleaseFull, startWith: ReleaseFull.Episode)

    @StateStrategyType(SkipStrategy::class)
    fun playWeb(link: String)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisode(release: ReleaseFull, episode: ReleaseFull.Episode, playFlag: Int? = null, quality: Int? = null)
}
