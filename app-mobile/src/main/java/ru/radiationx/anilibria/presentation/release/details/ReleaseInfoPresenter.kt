package ru.radiationx.anilibria.presentation.release.details

import android.util.Log
import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.toPrefQuality
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.TorrentItem
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.data.repository.VitalRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class ReleaseInfoPresenter @Inject constructor(
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val vitalRepository: VitalRepository,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val router: Router,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler,
    private val apiConfig: ApiConfig,
    private val authMainAnalytics: AuthMainAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val webPlayerAnalytics: WebPlayerAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val playerAnalytics: PlayerAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics
) : BasePresenter<ReleaseInfoView>(router) {

    private var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(ReleaseFull(it))
        }
        observeRelease()
        loadRelease()
        loadVital()
        subscribeAuth()
    }

    fun getQuality() = releaseInteractor.getQuality()

    fun setQuality(value: Int) = releaseInteractor.setQuality(value)

    fun getPlayerType() = releaseInteractor.getPlayerType()

    fun setPlayerType(value: Int) = releaseInteractor.setPlayerType(value)


    private fun subscribeAuth() {
        authRepository
            .observeUser()
            .distinctUntilChanged()
            .skip(1)
            .subscribe {
                loadRelease()
            }
            .addToDisposable()
    }

    private fun loadVital() {
        vitalRepository
            .observeByRule(VitalItem.Rule.RELEASE_DETAIL)
            .subscribe { vitals ->
                vitals.filter { it.type == VitalItem.VitalType.CONTENT_ITEM }.let {
                    if (it.isNotEmpty()) {
                        viewState.showVitalItems(it)
                    }
                }
            }
            .addToDisposable()
    }

    private fun loadRelease() {
        releaseInteractor
            .loadRelease(releaseId, releaseIdCode)
            .doOnSubscribe { viewState.setRefreshing(true) }
            .subscribe({ release ->
                viewState.setRefreshing(false)
                historyRepository.putRelease(release as ReleaseItem)
            }) {
                viewState.setRefreshing(false)
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    private fun observeRelease() {
        releaseInteractor
            .observeFull(releaseId, releaseIdCode)
            .subscribe({ release ->
                updateLocalRelease(release)
            }) {
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    private fun updateLocalRelease(release: ReleaseFull) {
        currentData = release
        releaseId = release.id
        releaseIdCode = release.code
        viewState.showRelease(release)
    }

    fun markEpisodeViewed(episode: ReleaseFull.Episode) {
        episode.isViewed = true
        episode.lastAccess = System.currentTimeMillis()
        releaseInteractor.putEpisode(episode)
    }

    fun markEpisodeUnviewed(episode: ReleaseFull.Episode) {
        releaseAnalytics.historyResetEpisode()
        episode.isViewed = false
        episode.lastAccess = 0
        releaseInteractor.putEpisode(episode)
    }

    fun onTorrentClick() {
        currentData?.let {
            when {
                it.torrents.size == 1 -> viewState.loadTorrent(it.torrents.last())
                else -> viewState.showTorrentDialog(it.torrents)
            }
        }
    }

    fun onTorrentClick(item: TorrentItem) {
        currentData?.let {
            val isHevc = item.quality?.contains("HEVC", true) == true
            releaseAnalytics.torrentClick(isHevc, it.id)
            viewState.loadTorrent(item)
        }

    }

    fun onCommentsClick() {
        currentData?.also {
            releaseAnalytics.commentsClick(it.id)
        }
    }

    fun onClickWatchWeb(place: EpisodeControlPlace) {
        currentData?.let { release ->
            releaseAnalytics.webPlayerClick(release.id)
            release.moonwalkLink?.let {
                viewState.playWeb(it, release.code.orEmpty())
            }
        }
    }

    fun onPlayAllClick(place: EpisodeControlPlace) {
        currentData?.let {
            place.handle({
                releaseAnalytics.episodesTopStartClick(it.id)
            }, {
                releaseAnalytics.episodesStartClick(it.id)
            })
            viewState.playEpisodes(it)
        }
    }

    fun onClickContinue(place: EpisodeControlPlace) {
        currentData?.also { release ->
            place.handle({
                releaseAnalytics.episodesTopContinueClick(release.id)
            }, {
                releaseAnalytics.episodesContinueClick(release.id)
            })
            Log.e("jojojo", release.episodes.joinToString { "${it.id}=>${it.lastAccess}" })
            release.episodes.asReversed().maxBy { it.lastAccess }?.let { episode ->
                viewState.playContinue(release, episode)
            }
        }
    }

    fun submitPlayerOpenAnalytics(playerType: AnalyticsPlayer, quality: AnalyticsQuality) {
        playerAnalytics.open(AnalyticsConstants.screen_release, playerType, quality)
    }

    fun onClickEpisodesMenu(place: EpisodeControlPlace) {
        currentData?.also { viewState.showEpisodesMenuDialog() }
    }

    fun onPlayEpisodeClick(
        episode: ReleaseFull.Episode,
        playFlag: Int? = null,
        quality: Int? = null
    ) {
        currentData?.let {
            val analyticsQuality =
                quality?.toPrefQuality()?.toAnalyticsQuality() ?: AnalyticsQuality.NONE
            when (episode.type) {
                ReleaseFull.Episode.Type.ONLINE -> {
                    releaseAnalytics.episodePlayClick(analyticsQuality, it.id)
                }
                ReleaseFull.Episode.Type.SOURCE -> {
                    releaseAnalytics.episodeDownloadClick(analyticsQuality, it.id)
                }
            }
            viewState.playEpisode(it, episode, playFlag, quality)
        }
    }

    fun onLongClickEpisode(episode: ReleaseFull.Episode) {
        currentData?.also { viewState.showLongPressEpisodeDialog(episode) }
    }

    fun onClickLink(url: String) {
        currentData?.also {
            releaseAnalytics.descriptionLinkClick(it.id)
            val handled = linkHandler.handle(url, router)
            if (!handled) {
                Utils.externalLink(url)
            }
        }
    }

    fun onClickDonate() {
        currentData?.also {
            releaseAnalytics.donateClick(it.id)
            donationDetailAnalytics.open(AnalyticsConstants.screen_release)
            router.navigateTo(Screens.DonationDetail())
        }
    }

    fun onClickFav() {
        if (authRepository.getAuthState() != AuthState.AUTH) {
            viewState.showFavoriteDialog()
            return
        }
        val releaseId = currentData?.id ?: return
        val favInfo = currentData?.favoriteInfo ?: return

        if (favInfo.isAdded) {
            releaseAnalytics.favoriteRemove(releaseId)
        } else {
            releaseAnalytics.favoriteAdd(releaseId)
        }

        val source = if (favInfo.isAdded) {
            favoriteRepository.deleteFavorite(releaseId)
        } else {
            favoriteRepository.addFavorite(releaseId)
        }

        source
            .doOnSubscribe {
                favInfo.inProgress = true
                viewState.updateFavCounter()
            }
            .doAfterTerminate {
                favInfo.inProgress = false
                viewState.updateFavCounter()
            }
            .subscribe({ releaseItem ->
                favInfo.rating = releaseItem.favoriteInfo.rating
                favInfo.isAdded = releaseItem.favoriteInfo.isAdded
                viewState.updateFavCounter()
            }) {
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    fun onScheduleClick(day: Int) {
        currentData?.also {
            releaseAnalytics.scheduleClick(it.id)
        }
        scheduleAnalytics.open(AnalyticsConstants.screen_release)
        router.navigateTo(Screens.Schedule(day))
    }

    fun onDescriptionExpandChanged(isExpanded: Boolean) {
        currentData?.also {
            if (isExpanded) {
                releaseAnalytics.descriptionExpand(it.id)
            }
        }
    }

    fun openAuth() {
        authMainAnalytics.open(AnalyticsConstants.screen_release)
        router.navigateTo(Screens.Auth())
    }

    fun openSearch(genre: String) {
        currentData?.also {
            releaseAnalytics.genreClick(it.id)
        }
        catalogAnalytics.open(AnalyticsConstants.screen_release)
        router.navigateTo(Screens.ReleasesSearch(genre))
    }

    fun onDownloadLinkSelected(url: String) {
        currentData?.also {
            if (it.showDonateDialog) {
                viewState.showFileDonateDialog(url)
            } else {
                viewState.showDownloadDialog(url)
            }
        }
    }

    fun submitDownloadEpisodeUrlAnalytics() {
        currentData?.also {
            releaseAnalytics.episodeDownloadByUrl(it.id)
        }
    }

    fun onDialogPatreonClick() {
        Utils.externalLink("https://www.patreon.com/anilibria")
    }

    fun onDialogDonateClick() {
        router.navigateTo(Screens.DonationDetail())
    }

    fun onResetEpisodesHistoryClick() {
        releaseAnalytics.historyReset()
        currentData?.also {
            releaseInteractor.resetEpisodesHistory(it.id)
        }
    }

    fun onCheckAllEpisodesHistoryClick() {
        releaseAnalytics.historyViewAll()
        currentData?.also {
            it.episodes.forEach {
                it.isViewed = true
            }
            releaseInteractor.putEpisodes(it.episodes)
        }
    }

    fun onWebPlayerClick() {
        webPlayerAnalytics.open(AnalyticsConstants.screen_release, releaseId)
    }

    private fun EpisodeControlPlace.handle(topListener: () -> Unit, bottomListener: () -> Unit) {
        when (this) {
            EpisodeControlPlace.TOP -> topListener()
            EpisodeControlPlace.BOTTOM -> bottomListener()
        }
    }

}