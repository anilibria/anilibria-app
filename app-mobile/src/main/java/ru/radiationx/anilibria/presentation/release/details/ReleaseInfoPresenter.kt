package ru.radiationx.anilibria.presentation.release.details

import android.util.Log
import moxy.InjectViewState
import ru.radiationx.anilibria.model.loading.StateController
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
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.ExternalEpisode
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.SourceEpisode
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class ReleaseInfoPresenter @Inject constructor(
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val router: Router,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler,
    private val appPreferences: PreferencesHolder,
    private val authMainAnalytics: AuthMainAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val webPlayerAnalytics: WebPlayerAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val playerAnalytics: PlayerAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics
) : BasePresenter<ReleaseInfoView>(router) {

    private val remindText =
        "Если серии всё ещё нет в плеере, воспользуйтесь торрентом или веб-плеером"

    private var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    private val stateController = StateController(ReleaseDetailScreenState())

    private fun updateModifiers(block: (ReleaseDetailModifiersState) -> ReleaseDetailModifiersState) {
        stateController.updateState {
            it.copy(
                modifiers = block.invoke(it.modifiers)
            )
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        stateController
            .observeState()
            .subscribe { viewState.showState(it) }
            .addToDisposable()

        appPreferences
            .observeEpisodesIsReverse()
            .subscribe { episodesReversed ->
                updateModifiers {
                    it.copy(episodesReversed = episodesReversed)
                }

            }
            .addToDisposable()

        appPreferences
            .observeReleaseRemind()
            .subscribe { remindEnabled ->
                stateController.updateState {
                    it.copy(remindText = remindText.takeIf { remindEnabled })
                }
            }
            .addToDisposable()

        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(ReleaseFull(it))
        }
        observeRelease()
        loadRelease()
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

    private fun loadRelease() {
        releaseInteractor
            .loadRelease(releaseId, releaseIdCode)
            .subscribe({ release ->
                historyRepository.putRelease(release as ReleaseItem)
            }) {
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
        stateController.updateState {
            it.copy(data = release.toState())
        }
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

    fun onEpisodeTabClick(tabTag: String) {
        currentData?.let { release ->
            releaseAnalytics.episodesTabClick(release.id, tabTag)
        }
        updateModifiers {
            it.copy(selectedEpisodesTabTag = tabTag)
        }
    }

    fun onRemindCloseClick() {
        appPreferences.releaseRemind = false
    }

    fun onTorrentClick(item: ReleaseTorrentItemState) {
        val torrentItem = currentData?.torrents?.find { it.id == item.id } ?: return
        val isHevc = torrentItem.quality?.contains("HEVC", true) == true
        releaseAnalytics.torrentClick(isHevc, torrentItem.id)
        viewState.loadTorrent(torrentItem)
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

    private fun onExternalEpisodeClick(
        episodeState: ReleaseEpisodeItemState,
        release: ReleaseFull,
        episode: ExternalEpisode
    ) {
        releaseAnalytics.episodeExternalClick(release.id, episodeState.tag)
        episode.url?.also { Utils.externalLink(it) }
    }

    private fun onSourceEpisodeClick(
        release: ReleaseFull,
        episode: SourceEpisode,
        quality: Int? = null
    ) {
        val analyticsQuality =
            quality?.toPrefQuality()?.toAnalyticsQuality() ?: AnalyticsQuality.NONE
        releaseAnalytics.episodeDownloadClick(analyticsQuality, release.id)
        viewState.downloadEpisode(episode, quality)
    }

    private fun onOnlineEpisodeClick(
        release: ReleaseFull,
        episode: ReleaseFull.Episode,
        playFlag: Int? = null,
        quality: Int? = null
    ) {
        val analyticsQuality =
            quality?.toPrefQuality()?.toAnalyticsQuality() ?: AnalyticsQuality.NONE
        releaseAnalytics.episodePlayClick(analyticsQuality, release.id)
        viewState.playEpisode(release, episode, playFlag, quality)
    }

    fun onEpisodeClick(
        episodeState: ReleaseEpisodeItemState,
        playFlag: Int? = null,
        quality: Int? = null
    ) {
        val release = currentData ?: return
        when (episodeState.type) {
            ReleaseEpisodeItemType.ONLINE -> {
                val episodeItem = getEpisodeItem(episodeState) ?: return
                onOnlineEpisodeClick(release, episodeItem, playFlag, quality)
            }
            ReleaseEpisodeItemType.SOURCE -> {
                val episodeItem = getSourceEpisode(episodeState) ?: return
                onSourceEpisodeClick(release, episodeItem, quality)
            }
            ReleaseEpisodeItemType.EXTERNAL -> {
                val episodeItem = getExternalEpisode(episodeState) ?: return
                onExternalEpisodeClick(episodeState, release, episodeItem)
            }
        }
    }

    fun onLongClickEpisode(episode: ReleaseEpisodeItemState) {
        val episodeItem = getEpisodeItem(episode) ?: return
        viewState.showLongPressEpisodeDialog(episodeItem)
    }

    private fun getEpisodeItem(episode: ReleaseEpisodeItemState): ReleaseFull.Episode? {
        if (episode.type != ReleaseEpisodeItemType.ONLINE) return null
        return currentData?.episodes?.find { it.id == episode.id }
    }

    private fun getSourceEpisode(episode: ReleaseEpisodeItemState): SourceEpisode? {
        if (episode.type != ReleaseEpisodeItemType.SOURCE) return null
        return currentData?.sourceEpisodes?.find { it.id == episode.id }
    }

    private fun getExternalEpisode(episode: ReleaseEpisodeItemState): ExternalEpisode? {
        if (episode.type != ReleaseEpisodeItemType.EXTERNAL) return null
        val release = currentData ?: return null
        return release.externalPlaylists
            .find { it.tag == episode.tag }
            ?.episodes
            ?.find { it.id == episode.id }
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
                updateModifiers {
                    it.copy(favoriteRefreshing = true)
                }
            }
            .doAfterTerminate {
                updateModifiers {
                    it.copy(favoriteRefreshing = false)
                }
            }
            .subscribe({ releaseItem ->
                favInfo.rating = releaseItem.favoriteInfo.rating
                favInfo.isAdded = releaseItem.favoriteInfo.isAdded
                stateController.updateState {
                    it.copy(
                        data = it.data?.copy(
                            info = it.data.info.copy(
                                favorite = releaseItem.favoriteInfo.toState()
                            )
                        )
                    )
                }
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

    fun onDescriptionExpandClick() {
        updateModifiers {
            it.copy(descriptionExpanded = !it.descriptionExpanded)
        }
        currentData?.also {
            if (stateController.currentState.modifiers.descriptionExpanded) {
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