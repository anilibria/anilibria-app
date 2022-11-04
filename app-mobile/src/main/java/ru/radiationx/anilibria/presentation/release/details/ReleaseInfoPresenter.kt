package ru.radiationx.anilibria.presentation.release.details

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.DonationCardItemState
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
import ru.radiationx.data.entity.app.release.*
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.DonationRepository
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
    private val donationRepository: DonationRepository,
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
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val teamsAnalytics: TeamsAnalytics
) : BasePresenter<ReleaseInfoView>(router) {

    private val remindText =
        "Если серии всё ещё нет в плеере, воспользуйтесь торрентом или веб-плеером"

    private var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    private val stateController = StateController(
        ReleaseDetailScreenState()
    )

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
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

        donationRepository
            .observerDonationInfo()
            .onEach { info ->
                stateController.updateState { state ->
                    val newCardState = info.cardRelease?.let {
                        DonationCardItemState(
                            tag = "donate",
                            title = it.title,
                            subtitle = it.subtitle,
                            canClose = false
                        )
                    }
                    state.copy(donationCardState = newCardState)
                }
            }
            .launchIn(presenterScope)

        appPreferences
            .observeEpisodesIsReverse()
            .onEach { episodesReversed ->
                updateModifiers {
                    it.copy(episodesReversed = episodesReversed)
                }
            }
            .launchIn(presenterScope)

        appPreferences
            .observeReleaseRemind()
            .onEach { remindEnabled ->
                stateController.updateState {
                    it.copy(remindText = remindText.takeIf { remindEnabled })
                }
            }
            .launchIn(presenterScope)

        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(ReleaseFull.emptyBy(it))
        }
        observeRelease()
    }

    fun getQuality() = releaseInteractor.getQuality()

    fun setQuality(value: Int) = releaseInteractor.setQuality(value)

    fun getPlayerType() = releaseInteractor.getPlayerType()

    fun setPlayerType(value: Int) = releaseInteractor.setPlayerType(value)

    private fun observeRelease() {
        releaseInteractor
            .observeFull(releaseId, releaseIdCode)
            .onEach { updateLocalRelease(it) }
            .launchIn(presenterScope)
    }

    private fun updateLocalRelease(release: ReleaseFull) {
        currentData = release
        releaseId = release.id
        releaseIdCode = release.code
        stateController.updateState {
            it.copy(data = release.toState())
        }
    }

    fun markEpisodeViewed(episode: Episode) {
        releaseInteractor.putEpisode(
            episode.access.copy(
                isViewed = true,
                lastAccess = System.currentTimeMillis()
            )
        )
    }

    fun markEpisodeUnviewed(episode: Episode) {
        releaseAnalytics.historyResetEpisode()
        releaseInteractor.putEpisode(
            episode.access.copy(
                isViewed = false,
                lastAccess = 0
            )
        )
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
            release.episodes.asReversed().maxByOrNull { it.access.lastAccess }?.let { episode ->
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

    private fun onRutubeEpisodeClick(
        release: ReleaseFull,
        episode: RutubeEpisode
    ) {
        releaseAnalytics.episodeRutubeClick(release.id)
        viewState.playWeb(episode.url, release.code.orEmpty())
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
        episode: Episode,
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
            ReleaseEpisodeItemType.RUTUBE -> {
                val episodeItem = getRutubeEpisode(episodeState) ?: return
                onRutubeEpisodeClick(release, episodeItem)
            }
        }
    }

    fun onLongClickEpisode(episode: ReleaseEpisodeItemState) {
        val episodeItem = getEpisodeItem(episode) ?: return
        viewState.showLongPressEpisodeDialog(episodeItem)
    }

    private fun getEpisodeItem(episode: ReleaseEpisodeItemState): Episode? {
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

    private fun getRutubeEpisode(episode: ReleaseEpisodeItemState): RutubeEpisode? {
        if (episode.type != ReleaseEpisodeItemType.RUTUBE) return null
        return currentData?.rutubePlaylist?.find { it.id == episode.id }
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

        presenterScope.launch {
            updateModifiers {
                it.copy(favoriteRefreshing = true)
            }
            runCatching {
                if (favInfo.isAdded) {
                    favoriteRepository.deleteFavorite(releaseId)
                } else {
                    favoriteRepository.addFavorite(releaseId)
                }
            }.onSuccess { releaseItem ->
                currentData?.also { data ->
                    val newData = data.copy(
                        item = data.item.copy(
                            favoriteInfo = releaseItem.favoriteInfo
                        )
                    )
                    releaseInteractor.updateFullCache(newData)
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            updateModifiers {
                it.copy(favoriteRefreshing = false)
            }
        }
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

    fun openSearch(tag: String, index: Int) {
        val data = currentData ?: return
        when (tag) {
            ReleaseInfoState.TAG_GENRE -> {
                val genre = data.genres.getOrNull(index) ?: return
                releaseAnalytics.genreClick(data.id)
                catalogAnalytics.open(AnalyticsConstants.screen_release)
                router.navigateTo(Screens.ReleasesSearch(genre))
            }
            ReleaseInfoState.TAG_VOICE -> {
                val voice = data.voices.getOrNull(index) ?: return
                releaseAnalytics.voiceClick(data.id)
                teamsAnalytics.open(AnalyticsConstants.screen_release)
                router.navigateTo(Screens.Teams(voice))
            }
        }
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
            val accesses = it.episodes.map {
                it.access.copy(isViewed = true)
            }
            releaseInteractor.putEpisodes(accesses)
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