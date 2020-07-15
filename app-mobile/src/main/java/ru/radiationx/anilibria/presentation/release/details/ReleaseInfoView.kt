package ru.radiationx.anilibria.presentation.release.details

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.TorrentItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseInfoView: IBaseView {
    fun showVitalItems(vital: List<VitalItem>)

    fun updateFavCounter()

    fun showRelease(release: ReleaseFull)

    @StateStrategyType(SkipStrategy::class)
    fun loadTorrent(torrent: TorrentItem)

    @StateStrategyType(SkipStrategy::class)
    fun showTorrentDialog(torrents: List<TorrentItem>)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisodes(release: ReleaseFull)

    @StateStrategyType(SkipStrategy::class)
    fun playContinue(release: ReleaseFull, startWith: ReleaseFull.Episode)

    @StateStrategyType(SkipStrategy::class)
    fun playWeb(link: String)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisode(release: ReleaseFull, episode: ReleaseFull.Episode, playFlag: Int? = null, quality: Int? = null)

    @StateStrategyType(SkipStrategy::class)
    fun showFavoriteDialog()

    @StateStrategyType(SkipStrategy::class)
    fun showDownloadDialog(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showFileDonateDialog(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showEpisodesMenuDialog()
}