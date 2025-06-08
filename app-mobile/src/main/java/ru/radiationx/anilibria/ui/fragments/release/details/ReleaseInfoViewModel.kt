package ru.radiationx.anilibria.ui.fragments.release.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import ru.radiationx.anilibria.ads.NativeAdsRepository
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.PlayerAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.analytics.features.TeamsAnalytics
import ru.radiationx.data.analytics.features.WebPlayerAnalytics
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.data.api.favorites.FavoriteRepository
import ru.radiationx.data.api.favorites.FavoritesInteractor
import ru.radiationx.data.api.releases.ReleaseInteractor
import ru.radiationx.data.api.releases.models.Episode
import ru.radiationx.data.api.releases.models.ExternalEpisode
import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.releases.models.ReleaseSponsor
import ru.radiationx.data.api.releases.models.RutubeEpisode
import ru.radiationx.data.api.schedule.models.PublishDay
import ru.radiationx.data.api.torrents.models.TorrentItem
import ru.radiationx.data.app.ads.AdsConfigRepository
import ru.radiationx.data.app.donation.DonationRepository
import ru.radiationx.data.app.downloader.RemoteFileRepository
import ru.radiationx.data.app.downloader.mapper.toLocalFile
import ru.radiationx.data.app.downloader.models.LocalFile
import ru.radiationx.data.app.downloader.models.RemoteFile
import ru.radiationx.data.app.episodeaccess.models.EpisodeAccess
import ru.radiationx.data.app.preferences.PreferencesHolder
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.TorrentId
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import javax.inject.Inject

class ReleaseInfoViewModel @Inject constructor(
    private val argExtra: ReleaseExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    donationRepository: DonationRepository,
    private val router: Router,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler,
    private val systemUtils: SystemUtils,
    private val appPreferences: PreferencesHolder,
    private val commentsNotifier: ReleaseCommentsNotifier,
    private val authMainAnalytics: AuthMainAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val webPlayerAnalytics: WebPlayerAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val playerAnalytics: PlayerAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val teamsAnalytics: TeamsAnalytics,
    private val remoteFileRepository: RemoteFileRepository,
    private val adsConfigRepository: AdsConfigRepository,
    private val nativeAdsRepository: NativeAdsRepository,
) : ViewModel() {

    private val remindText =
        "Если серии всё ещё нет в плеере, воспользуйтесь торрентом или веб-плеером"

    private var currentData: Release? = null

    private val _state = MutableStateFlow(ReleaseDetailScreenState())
    val state = _state.asStateFlow()

    private val loadingJobs = mutableMapOf<TorrentId, Job>()
    private val _currentLoadings =
        MutableStateFlow<Map<TorrentId, MutableStateFlow<Int>>>(emptyMap())

    val playWebAction = EventFlow<ActionPlayWeb>()
    val playEpisodeAction = EventFlow<ActionPlayEpisode>()
    val showUnauthAction = EventFlow<Unit>()
    val showEpisodesMenuAction = EventFlow<Unit>()
    val showContextEpisodeAction = EventFlow<Episode>()
    val openDownloadedFileAction = EventFlow<LocalFile>()
    val shareDownloadedFileAction = EventFlow<LocalFile>()

    private fun updateModifiers(block: (ReleaseDetailModifiersState) -> ReleaseDetailModifiersState) {
        _state.update {
            it.copy(
                modifiers = block.invoke(it.modifiers)
            )
        }
    }

    init {
        donationRepository
            .observerDonationInfo()
            .onEach { info ->
                _state.update { state ->
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
            .launchIn(viewModelScope)

        appPreferences
            .episodesIsReverse
            .onEach { episodesReversed ->
                updateModifiers {
                    it.copy(episodesReversed = episodesReversed)
                }
            }
            .launchIn(viewModelScope)

        appPreferences
            .releaseRemind
            .onEach { remindEnabled ->
                _state.update {
                    it.copy(remindText = remindText.takeIf { remindEnabled })
                }
            }
            .launchIn(viewModelScope)

        argExtra.release?.also {
            updateLocalRelease(it, _currentLoadings.value, emptyMap(), emptySet())
        }
        releaseInteractor.getItem(argExtra.id)?.also {
            updateLocalRelease(it, _currentLoadings.value, emptyMap(), emptySet())
        }
        observeRelease()

        viewModelScope.launch {
            coRunCatching {
                val config = adsConfigRepository.getConfig().releaseNative
                if (!config.enabled) return@coRunCatching
                val releaseTags = _state.mapNotNull { it.data?.info }.first().let {
                    listOf(it.titleRus, it.titleEng)
                }
                val contextTags = config.contextTags + releaseTags
                val request = NativeAdRequestConfiguration.Builder(config.unitId)
                    .setContextTags(contextTags)
                    .build()
                val nativeAd = withTimeout(config.timeoutMillis) {
                    nativeAdsRepository.load(request)
                }
                _state.update { it.copy(nativeAd = nativeAd) }
            }.onFailure {
                Timber.e(it, "Error while load ads for release")
            }
        }
        viewModelScope.launch {
            authRepository.observeAuthState().filter { it == AuthState.AUTH }.first()
            updateModifiers { it.copy(favoriteLoading = true) }
            coRunCatching {
                favoritesInteractor.loadReleaseIds()
            }.onFailure {
                Timber.e(it, "Error while favorites release ids")
            }
            updateModifiers { it.copy(favoriteLoading = false) }
        }
    }

    private fun observeRelease() {
        updateModifiers {
            it.copy(detailLoading = true)
        }
        releaseInteractor
            .observeFull(argExtra.id)
            .flatMapLatest { release ->
                combine(
                    _currentLoadings,
                    releaseInteractor.observeAccesses(release.id).map { accesses ->
                        accesses.associateBy { it.id }
                    },
                    favoritesInteractor.observeIds()
                ) { loadings, accesses, favoriteIds ->
                    updateLocalRelease(release, loadings, accesses, favoriteIds)
                }
            }
            .onEach {
                updateModifiers {
                    it.copy(detailLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateLocalRelease(
        release: Release,
        loadings: Map<TorrentId, MutableStateFlow<Int>>,
        accesses: Map<EpisodeId, EpisodeAccess>,
        favoriteIds: Set<ReleaseId>
    ) {
        currentData = release
        _state.update {
            it.copy(data = release.toState(loadings, accesses, favoriteIds.contains(release.id)))
        }
    }

    fun markEpisodeUnviewed(episode: Episode) {
        viewModelScope.launch {
            releaseAnalytics.historyResetEpisode()
            releaseInteractor.markUnViewed(episode.id)
        }
    }

    fun onEpisodeTabClick(tabTag: String) {
        currentData?.let { release ->
            releaseAnalytics.episodesTabClick(release.id.id, tabTag)
        }
        updateModifiers {
            it.copy(selectedEpisodesTabTag = tabTag)
        }
    }

    fun onRemindCloseClick() {
        appPreferences.releaseRemind.value = false
    }

    fun onTorrentClick(id: TorrentId, action: TorrentAction) {
        val torrentItem = currentData?.torrents?.find { it.id == id } ?: return
        val isHevc = torrentItem.codec?.contains("HEVC", true) == true
        releaseAnalytics.torrentClick(isHevc, torrentItem.id.id)
        when (action) {
            TorrentAction.Open, TorrentAction.Share -> loadTorrent(torrentItem, action)
            TorrentAction.OpenUrl -> systemUtils.open(torrentItem.url)
            TorrentAction.ShareUrl -> systemUtils.share(torrentItem.url)
        }
    }

    fun onCancelTorrentClick(ud: TorrentId) {
        loadingJobs[ud]?.cancel()
        loadingJobs.remove(ud)
        _currentLoadings.update { it.minus(ud) }
    }

    private fun loadTorrent(item: TorrentItem, action: TorrentAction) {
        if (loadingJobs[item.id]?.isActive == true) {
            return
        }
        loadingJobs[item.id] = viewModelScope.launch {
            val progress = MutableStateFlow(0)
            _currentLoadings.update {
                it.plus(item.id to progress)
            }
            coRunCatching {
                val bucket = RemoteFile.Bucket.Torrent(item.id.releaseId)
                remoteFileRepository.loadFile(item.url, bucket, progress)
            }.onSuccess {
                when (action) {
                    TorrentAction.Open -> openDownloadedFileAction.set(it.toLocalFile())
                    TorrentAction.Share -> shareDownloadedFileAction.set(it.toLocalFile())
                    TorrentAction.OpenUrl, TorrentAction.ShareUrl -> {
                        // do nothing
                    }
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            _currentLoadings.update {
                it.minus(item.id)
            }
        }
    }

    fun onCommentsClick() {
        currentData?.also {
            releaseAnalytics.commentsClick(it.id.id)
        }
        commentsNotifier.requireOpen()
    }

    fun onClickWatchWeb() {
        currentData?.also { release ->
            releaseAnalytics.webPlayerClick(release.id.id)
            release.webPlayer?.let {
                playWebAction.set(ActionPlayWeb(it, release.alias.alias))
            }
        }
    }

    fun onPlayAllClick(place: EpisodeControlPlace) {
        val release = currentData ?: return
        place.handle({
            releaseAnalytics.episodesTopStartClick(release.id.id)
        }, {
            releaseAnalytics.episodesStartClick(release.id.id)
        })
        release.episodes.firstOrNull()?.also {
            playEpisodeAction.set(ActionPlayEpisode(it.id))
        }
    }

    fun onClickContinue(place: EpisodeControlPlace) {
        val release = currentData ?: return
        place.handle({
            releaseAnalytics.episodesTopContinueClick(release.id.id)
        }, {
            releaseAnalytics.episodesContinueClick(release.id.id)
        })
        viewModelScope.launch {
            releaseInteractor.getAccesses(release.id).maxByOrNull { it.lastAccessRaw }?.also {
                playEpisodeAction.set(ActionPlayEpisode(it.id))
            }
        }
    }

    fun submitPlayerOpenAnalytics(episodeId: EpisodeId) {
        val quality = appPreferences.playerQuality.value.toAnalyticsQuality()
        playerAnalytics.open(
            AnalyticsConstants.screen_release,
            AnalyticsPlayer.INTERNAL,
            quality,
            episodeId
        )
    }

    fun onClickEpisodesMenu() {
        currentData?.also {
            showEpisodesMenuAction.set(Unit)
        }
    }

    private fun onRutubeEpisodeClick(
        release: Release,
        episode: RutubeEpisode,
    ) {
        releaseAnalytics.episodeRutubeClick(release.id.id)
        playWebAction.set(ActionPlayWeb(episode.url, release.alias.alias))
    }

    private fun onExternalEpisodeClick(
        episodeState: ReleaseEpisodeItemState,
        release: Release,
        episode: ExternalEpisode,
    ) {
        releaseAnalytics.episodeExternalClick(release.id.id, episodeState.tag)
        episode.url?.also { systemUtils.open(it) }
    }

    private fun onOnlineEpisodeClick(
        release: Release,
        episode: Episode,
    ) {
        val savedQuality = appPreferences.playerQuality.value
        val analyticsQuality = savedQuality.toAnalyticsQuality()
        releaseAnalytics.episodePlayClick(analyticsQuality, release.id.id)
        playEpisodeAction.set(ActionPlayEpisode(episode.id))
    }

    fun onEpisodeClick(
        episodeState: ReleaseEpisodeItemState,
        quality: PlayerQuality?,
    ) {
        val release = currentData ?: return
        when (episodeState.type) {
            ReleaseEpisodeItemType.ONLINE -> {
                val episodeItem = getEpisodeItem(episodeState) ?: return
                onOnlineEpisodeClick(release, episodeItem)
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
        showContextEpisodeAction.set(episodeItem)
    }

    private fun getEpisodeItem(episode: ReleaseEpisodeItemState): Episode? {
        if (episode.type != ReleaseEpisodeItemType.ONLINE) return null
        return currentData?.episodes?.find { it.id == episode.id }
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
            releaseAnalytics.descriptionLinkClick(it.id.id)
            val handled = linkHandler.handle(url, router)
            if (!handled) {
                systemUtils.open(url)
            }
        }
    }

    fun onSponsorClick(sponsor: ReleaseSponsor) {
        val data = currentData ?: return
        releaseAnalytics.sponsorClick(data.id.id, sponsor.title)
        sponsor.url?.also { systemUtils.open(it) }
    }

    fun onClickDonate() {
        currentData?.also {
            releaseAnalytics.donateClick(it.id.id)
            donationDetailAnalytics.open(AnalyticsConstants.screen_release)
            router.navigateTo(Screens.DonationDetail())
        }
    }

    fun onClickFav() {
        val releaseId = currentData?.id ?: return
        viewModelScope.launch {
            if (authRepository.getAuthState() != AuthState.AUTH) {
                showUnauthAction.set(Unit)
                return@launch
            }
            val isAdded = favoritesInteractor
                .observeIds()
                .first()
                .contains(releaseId)

            if (isAdded) {
                releaseAnalytics.favoriteRemove(releaseId.id)
            } else {
                releaseAnalytics.favoriteAdd(releaseId.id)
            }
            updateModifiers {
                it.copy(favoriteRefreshing = true)
            }
            coRunCatching {
                if (isAdded) {
                    favoritesInteractor.deleteRelease(releaseId)
                } else {
                    favoritesInteractor.addRelease(releaseId)
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            updateModifiers {
                it.copy(favoriteRefreshing = false)
            }
        }
    }

    fun onScheduleClick(day: PublishDay) {
        currentData?.also {
            releaseAnalytics.scheduleClick(it.id.id)
        }
        scheduleAnalytics.open(AnalyticsConstants.screen_release)
        router.navigateTo(Screens.Schedule(day))
    }

    fun onDescriptionExpandClick() {
        updateModifiers {
            it.copy(descriptionExpanded = !it.descriptionExpanded)
        }
        currentData?.also {
            if (_state.value.modifiers.descriptionExpanded) {
                releaseAnalytics.descriptionExpand(it.id.id)
            }
        }
    }

    fun openAuth() {
        authMainAnalytics.open(AnalyticsConstants.screen_release)
        router.navigateTo(Screens.Auth())
    }

    fun openSearch(tag: String, value: String) {
        val data = currentData ?: return
        when (tag) {
            ReleaseInfoState.TAG_GENRE -> {
                releaseAnalytics.genreClick(data.id.id)
                catalogAnalytics.open(AnalyticsConstants.screen_release)
                val genre = data.genres.find { it.name == value }
                router.navigateTo(Screens.Catalog(genre))
            }

            ReleaseInfoState.TAG_VOICE -> {
                releaseAnalytics.voiceClick(data.id.id)
                teamsAnalytics.open(AnalyticsConstants.screen_release)
                router.navigateTo(Screens.Teams(value))
            }
        }
    }

    fun onResetEpisodesHistoryClick() {
        viewModelScope.launch {
            releaseAnalytics.historyReset()
            currentData?.also {
                releaseInteractor.resetAccessHistory(it.id)
            }
        }
    }

    fun onCheckAllEpisodesHistoryClick() {
        val release = currentData ?: return
        viewModelScope.launch {
            releaseAnalytics.historyViewAll()
            releaseInteractor.markAllViewed(release.id)
        }
    }

    fun onWebPlayerClick() {
        currentData?.also {
            webPlayerAnalytics.open(AnalyticsConstants.screen_release, it.id.id)
        }
    }

    private fun EpisodeControlPlace.handle(topListener: () -> Unit, bottomListener: () -> Unit) {
        when (this) {
            EpisodeControlPlace.TOP -> topListener()
            EpisodeControlPlace.BOTTOM -> bottomListener()
        }
    }

}

enum class TorrentAction {
    Open,
    Share,
    OpenUrl,
    ShareUrl
}

data class ActionPlayWeb(
    val link: String,
    val alias: String,
)

data class ActionPlayEpisode(
    val id: EpisodeId,
)