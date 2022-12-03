package ru.radiationx.anilibria.presentation.release.details

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.mintrocket.lib.mintpermissions.flows.ext.isSuccess
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.toPrefQuality
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.release.*
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.regex.Pattern

@InjectConstructor
class ReleaseInfoViewModel(
    private val argExtra: ReleaseExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val donationRepository: DonationRepository,
    private val router: Router,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler,
    private val systemUtils: SystemUtils,
    private val appPreferences: PreferencesHolder,
    private val mintPermissionsDialogFlow: MintPermissionsDialogFlow,
    private val authMainAnalytics: AuthMainAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val webPlayerAnalytics: WebPlayerAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val playerAnalytics: PlayerAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val teamsAnalytics: TeamsAnalytics
) : ViewModel() {

    private val remindText =
        "Если серии всё ещё нет в плеере, воспользуйтесь торрентом или веб-плеером"

    private var currentData: Release? = null

    private val _state = MutableStateFlow(ReleaseDetailScreenState())
    val state = _state.asStateFlow()


    val loadTorrentAction = EventFlow<TorrentItem>()
    val playEpisodesAction = EventFlow<Release>()
    val playContinueAction = EventFlow<ActionContinue>()
    val playWebAction = EventFlow<ActionPlayWeb>()
    val playEpisodeAction = EventFlow<ActionPlayEpisode>()
    val loadEpisodeAction = EventFlow<ActionLoadEpisode>()
    val showUnauthAction = EventFlow<Unit>()
    val showDownloadAction = EventFlow<String>()
    val showFileDonateAction = EventFlow<String>()
    val showEpisodesMenuAction = EventFlow<Unit>()
    val showContextEpisodeAction = EventFlow<Episode>()

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
            .observeEpisodesIsReverse()
            .onEach { episodesReversed ->
                updateModifiers {
                    it.copy(episodesReversed = episodesReversed)
                }
            }
            .launchIn(viewModelScope)

        appPreferences
            .observeReleaseRemind()
            .onEach { remindEnabled ->
                _state.update {
                    it.copy(remindText = remindText.takeIf { remindEnabled })
                }
            }
            .launchIn(viewModelScope)

        releaseInteractor.getItem(argExtra.id, argExtra.code)?.also {
            updateLocalRelease(it)
        }
        observeRelease()
    }

    fun getQuality() = releaseInteractor.getQuality()

    fun setQuality(value: Int) = releaseInteractor.setQuality(value)

    fun getPlayerType() = releaseInteractor.getPlayerType()

    fun setPlayerType(value: Int) = releaseInteractor.setPlayerType(value)

    private fun observeRelease() {
        releaseInteractor
            .observeFull(argExtra.id, argExtra.code)
            .onEach { updateLocalRelease(it) }
            .launchIn(viewModelScope)
    }

    private fun updateLocalRelease(release: Release) {
        currentData = release
        _state.update {
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
            releaseAnalytics.episodesTabClick(release.id.id, tabTag)
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
        releaseAnalytics.torrentClick(isHevc, torrentItem.id.id)
        loadTorrentAction.set(torrentItem)
    }

    fun onCommentsClick() {
        currentData?.also {
            releaseAnalytics.commentsClick(it.id.id)
        }
    }

    fun onClickWatchWeb(place: EpisodeControlPlace) {
        currentData?.also { release ->
            releaseAnalytics.webPlayerClick(release.id.id)
            release.moonwalkLink?.let {
                playWebAction.set(ActionPlayWeb(it, release.code.code))
            }
        }
    }

    fun onPlayAllClick(place: EpisodeControlPlace) {
        currentData?.also {
            place.handle({
                releaseAnalytics.episodesTopStartClick(it.id.id)
            }, {
                releaseAnalytics.episodesStartClick(it.id.id)
            })
            playEpisodesAction.set(it)
        }
    }

    fun onClickContinue(place: EpisodeControlPlace) {
        currentData?.also { release ->
            place.handle({
                releaseAnalytics.episodesTopContinueClick(release.id.id)
            }, {
                releaseAnalytics.episodesContinueClick(release.id.id)
            })
            release.episodes.asReversed().maxByOrNull { it.access.lastAccess }?.let { episode ->
                playContinueAction.set(ActionContinue(release, episode))
            }
        }
    }

    fun submitPlayerOpenAnalytics(playerType: AnalyticsPlayer, quality: AnalyticsQuality) {
        playerAnalytics.open(AnalyticsConstants.screen_release, playerType, quality)
    }

    fun onClickEpisodesMenu(place: EpisodeControlPlace) {
        currentData?.also {
            showEpisodesMenuAction.set(Unit)
        }
    }

    private fun onRutubeEpisodeClick(
        release: Release,
        episode: RutubeEpisode
    ) {
        releaseAnalytics.episodeRutubeClick(release.id.id)
        playWebAction.set(ActionPlayWeb(episode.url, release.code.code))
    }

    private fun onExternalEpisodeClick(
        episodeState: ReleaseEpisodeItemState,
        release: Release,
        episode: ExternalEpisode
    ) {
        releaseAnalytics.episodeExternalClick(release.id.id, episodeState.tag)
        episode.url?.also { systemUtils.externalLink(it) }
    }

    private fun onSourceEpisodeClick(
        release: Release,
        episode: SourceEpisode,
        quality: Int? = null
    ) {
        val analyticsQuality =
            quality?.toPrefQuality()?.toAnalyticsQuality() ?: AnalyticsQuality.NONE
        releaseAnalytics.episodeDownloadClick(analyticsQuality, release.id.id)
        loadEpisodeAction.set(ActionLoadEpisode(episode, quality))
    }

    private fun onOnlineEpisodeClick(
        release: Release,
        episode: Episode,
        playFlag: Int? = null,
        quality: Int? = null
    ) {
        val analyticsQuality =
            quality?.toPrefQuality()?.toAnalyticsQuality() ?: AnalyticsQuality.NONE
        releaseAnalytics.episodePlayClick(analyticsQuality, release.id.id)
        playEpisodeAction.set(ActionPlayEpisode(release, episode, playFlag, quality))
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
        if (authRepository.getAuthState() != AuthState.AUTH) {
            showUnauthAction.set(Unit)
            return
        }
        val releaseId = currentData?.id ?: return
        val favInfo = currentData?.favoriteInfo ?: return

        if (favInfo.isAdded) {
            releaseAnalytics.favoriteRemove(releaseId.id)
        } else {
            releaseAnalytics.favoriteAdd(releaseId.id)
        }

        viewModelScope.launch {
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

    fun openSearch(tag: String, index: Int) {
        val data = currentData ?: return
        when (tag) {
            ReleaseInfoState.TAG_GENRE -> {
                val genre = data.genres.getOrNull(index) ?: return
                releaseAnalytics.genreClick(data.id.id)
                catalogAnalytics.open(AnalyticsConstants.screen_release)
                router.navigateTo(Screens.Catalog(genre))
            }
            ReleaseInfoState.TAG_VOICE -> {
                val voice = data.voices.getOrNull(index) ?: return
                releaseAnalytics.voiceClick(data.id.id)
                teamsAnalytics.open(AnalyticsConstants.screen_release)
                router.navigateTo(Screens.Teams(voice))
            }
        }
    }

    fun onDownloadLinkSelected(url: String) {
        currentData?.also {
            if (it.showDonateDialog) {
                showFileDonateAction.set(url)
            } else {
                showDownloadAction.set(url)
            }
        }
    }

    fun downloadFile(url: String) {
        var fileName = systemUtils.getFileNameFromUrl(url)
        val matcher = Pattern.compile("\\?download=([\\s\\S]+)").matcher(fileName)
        if (matcher.find()) {
            matcher.group(1)?.also {
                fileName = it
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            systemUtils.systemDownloader(url, fileName)
            return
        }
        viewModelScope.launch {
            val result =
                mintPermissionsDialogFlow.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result.isSuccess()) {
                systemUtils.systemDownloader(url, fileName)
            }
        }
    }

    fun submitDownloadEpisodeUrlAnalytics() {
        currentData?.also {
            releaseAnalytics.episodeDownloadByUrl(it.id.id)
        }
    }

    fun onDialogPatreonClick() {
        systemUtils.externalLink("https://www.patreon.com/anilibria")
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


data class ActionContinue(
    val release: Release,
    val startWith: Episode
)

data class ActionPlayWeb(
    val link: String,
    val code: String
)

data class ActionPlayEpisode(
    val release: Release,
    val episode: Episode,
    val playFlag: Int?,
    val quality: Int?
)

data class ActionLoadEpisode(
    val episode: SourceEpisode,
    val quality: Int?
)