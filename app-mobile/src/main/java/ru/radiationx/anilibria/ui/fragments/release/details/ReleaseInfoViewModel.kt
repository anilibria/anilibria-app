package ru.radiationx.anilibria.ui.fragments.release.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
import ru.radiationx.data.ads.AdsConfigRepository
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
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.downloader.LocalFile
import ru.radiationx.data.downloader.RemoteFile
import ru.radiationx.data.downloader.RemoteFileRepository
import ru.radiationx.data.downloader.toLocalFile
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.ExternalEpisode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.RutubeEpisode
import ru.radiationx.data.entity.domain.release.SourceEpisode
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.TorrentId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseInfoViewModel(
    private val argExtra: ReleaseExtra,
    private val releaseInteractor: ReleaseInteractor,
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
    val showFileDonateAction = EventFlow<String>()
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
            updateLocalRelease(it, _currentLoadings.value, emptyMap())
        }
        releaseInteractor.getItem(argExtra.id, argExtra.code)?.also {
            updateLocalRelease(it, _currentLoadings.value, emptyMap())
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
    }

    private fun observeRelease() {
        updateModifiers {
            it.copy(detailLoading = true)
        }
        releaseInteractor
            .observeFull(argExtra.id, argExtra.code)
            .onEach {
                updateModifiers {
                    it.copy(detailLoading = false)
                }
            }
            .flatMapLatest { release ->
                combine(
                    _currentLoadings,
                    releaseInteractor.observeAccesses(release.id).map { accesses ->
                        accesses.associateBy { it.id }
                    }
                ) { loadings, accesses ->
                    updateLocalRelease(release, loadings, accesses)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateLocalRelease(
        release: Release,
        loadings: Map<TorrentId, MutableStateFlow<Int>>,
        accesses: Map<EpisodeId, EpisodeAccess>,
    ) {
        currentData = release
        _state.update {
            it.copy(data = release.toState(loadings, accesses))
        }
    }

    fun markEpisodeUnviewed(episode: Episode) {
        viewModelScope.launch {
            releaseAnalytics.historyResetEpisode()
            releaseInteractor.markUnviewed(episode.id)
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
        val isHevc = torrentItem.quality?.contains("HEVC", true) == true
        releaseAnalytics.torrentClick(isHevc, torrentItem.id.id)
        when (action) {
            TorrentAction.Open, TorrentAction.Share -> loadTorrent(torrentItem, action)
            TorrentAction.OpenUrl -> systemUtils.externalLink(torrentItem.url.orEmpty())
            TorrentAction.ShareUrl -> systemUtils.shareText(torrentItem.url.orEmpty())
        }
    }

    fun onCancelTorrentClick(ud: TorrentId) {
        loadingJobs[ud]?.cancel()
        loadingJobs.remove(ud)
        _currentLoadings.update { it.minus(ud) }
    }

    private fun loadTorrent(item: TorrentItem, action: TorrentAction) {
        val url = item.url ?: return
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
                remoteFileRepository.loadFile(url, bucket, progress)
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
            release.moonwalkLink?.let {
                playWebAction.set(ActionPlayWeb(it, release.code.code))
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
        release.episodes.lastOrNull()?.also {
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
            releaseInteractor.getAccesses(release.id).maxByOrNull { it.lastAccess }?.also {
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
        playWebAction.set(ActionPlayWeb(episode.url, release.code.code))
    }

    private fun onExternalEpisodeClick(
        episodeState: ReleaseEpisodeItemState,
        release: Release,
        episode: ExternalEpisode,
    ) {
        releaseAnalytics.episodeExternalClick(release.id.id, episodeState.tag)
        episode.url?.also { systemUtils.externalLink(it) }
    }

    private fun onSourceEpisodeClick(
        release: Release,
        episode: SourceEpisode,
        quality: PlayerQuality?,
    ) {
        val savedQuality = appPreferences.playerQuality.value
        val finalQuality = quality ?: savedQuality
        val analyticsQuality = savedQuality.toAnalyticsQuality()
        releaseAnalytics.episodeDownloadClick(analyticsQuality, release.id.id)
        val url = episode.qualityInfo.getUrlFor(finalQuality) ?: return
        onDownloadLinkSelected(url)
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
        showContextEpisodeAction.set(episodeItem)
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
            releaseAnalytics.descriptionLinkClick(it.id.id)
            val handled = linkHandler.handle(url, router)
            if (!handled) {
                systemUtils.externalLink(url)
            }
        }
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
        val favInfo = currentData?.favoriteInfo ?: return
        viewModelScope.launch {
            if (authRepository.getAuthState() != AuthState.AUTH) {
                showUnauthAction.set(Unit)
                return@launch
            }

            if (favInfo.isAdded) {
                releaseAnalytics.favoriteRemove(releaseId.id)
            } else {
                releaseAnalytics.favoriteAdd(releaseId.id)
            }
            updateModifiers {
                it.copy(favoriteRefreshing = true)
            }
            coRunCatching {
                if (favInfo.isAdded) {
                    favoriteRepository.deleteFavorite(releaseId)
                } else {
                    favoriteRepository.addFavorite(releaseId)
                }
            }.onSuccess { releaseItem ->
                currentData?.also { data ->
                    val newData = data.copy(
                        favoriteInfo = releaseItem.favoriteInfo
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
                router.navigateTo(Screens.Catalog(value))
            }

            ReleaseInfoState.TAG_VOICE -> {
                releaseAnalytics.voiceClick(data.id.id)
                teamsAnalytics.open(AnalyticsConstants.screen_release)
                router.navigateTo(Screens.Teams(value))
            }
        }
    }

    fun onDownloadLinkSelected(url: String) {
        currentData?.also {
            if (it.showDonateDialog) {
                showFileDonateAction.set(url)
            } else {
                downloadFile(url)
            }
        }
    }

    fun downloadFile(url: String) {
        val data = currentData ?: return
        releaseAnalytics.episodeDownloadByUrl(data.id.id)
        systemUtils.externalLink(url)
    }

    fun onDialogPatreonClick() {
        systemUtils.externalLink("https://www.patreon.com/anilibria")
    }

    fun onDialogDonateClick() {
        router.navigateTo(Screens.DonationDetail())
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
    val code: String,
)

data class ActionPlayEpisode(
    val id: EpisodeId,
)