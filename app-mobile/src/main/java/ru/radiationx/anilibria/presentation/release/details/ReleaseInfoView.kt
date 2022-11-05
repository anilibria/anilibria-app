package ru.radiationx.anilibria.presentation.release.details

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.app.release.Episode
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.SourceEpisode
import ru.radiationx.data.entity.app.release.TorrentItem

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseInfoView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state:ReleaseDetailScreenState)

    @StateStrategyType(SkipStrategy::class)
    fun loadTorrent(torrent: TorrentItem)

    @StateStrategyType(SkipStrategy::class)
    fun showTorrentDialog(torrents: List<TorrentItem>)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisodes(release: ReleaseItem)

    @StateStrategyType(SkipStrategy::class)
    fun playContinue(release: ReleaseItem, startWith: Episode)

    @StateStrategyType(SkipStrategy::class)
    fun playWeb(link: String, code: String)

    @StateStrategyType(SkipStrategy::class)
    fun playEpisode(
        release: ReleaseItem,
        episode: Episode,
        playFlag: Int? = null,
        quality: Int? = null
    )

    @StateStrategyType(SkipStrategy::class)
    fun downloadEpisode(
        episode: SourceEpisode,
        quality: Int? = null
    )

    @StateStrategyType(SkipStrategy::class)
    fun showFavoriteDialog()

    @StateStrategyType(SkipStrategy::class)
    fun showDownloadDialog(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showFileDonateDialog(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showEpisodesMenuDialog()

    @StateStrategyType(SkipStrategy::class)
    fun showLongPressEpisodeDialog(episode: Episode)
}