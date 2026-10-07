package ru.radiationx.anilibria.screen.player

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.PlayerEndEpisodeGuidedScreen
import ru.radiationx.anilibria.screen.PlayerEndSeasonGuidedScreen
import ru.radiationx.anilibria.screen.PlayerEpisodesGuidedScreen
import ru.radiationx.anilibria.screen.PlayerQualityGuidedScreen
import ru.radiationx.anilibria.screen.PlayerSpeedGuidedScreen
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

class PlayerViewModel @Inject constructor(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val preferencesHolder: PreferencesHolder,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    val videoData = MutableStateFlow<Video?>(null)
    val qualityState = MutableStateFlow<PlayerQuality?>(null)
    val speedState = MutableStateFlow<Float?>(null)
    val playAction = EventFlow<Boolean>()

    private var currentEpisodes = mutableListOf<Episode>()
    private var currentReleases: List<Release>? = null
    private var currentEpisode: Episode? = null
    private var currentQuality: PlayerQuality? = null
    private var currentComplete: Boolean? = null
    private var currentDuration = 0L
    private var lastProgressSavedAt = 0L
    private var watchThresholdReached = false
    private var episodeReady = false
    private var episodeJob: Job? = null
    private var videoJob: Job? = null
    private var videoGeneration = 0L

    fun onPlaybackProgress(episodeId: EpisodeId, position: Long, duration: Long) {
        val episode = currentEpisode ?: return
        if (!episodeReady || episode.id != episodeId || position < 0L) return
        if (duration > 0L) currentDuration = duration
        val viewed = EpisodeWatchPolicy.isViewed(position, currentDuration, episode.skips?.ending?.start)
        val now = System.currentTimeMillis()
        if (viewed != watchThresholdReached || now - lastProgressSavedAt >= 10_000L) {
            saveEpisode(position)
        }
    }

    init {
        playerController.reset()
        qualityState.value = preferencesHolder.playerQuality.value
        speedState.value = preferencesHolder.playSpeed.value

        playerController
            .selectEpisodeRelay
            .onEach { episodeId ->
                currentEpisodes
                    .firstOrNull { it.id == episodeId }
                    ?.also { playEpisode(it, true) }
            }
            .launchIn(viewModelScope)

        preferencesHolder
            .playerQuality
            .onEach {
                currentQuality = it
                updateQuality()
                updateEpisode()
            }
            .launchIn(viewModelScope)

        preferencesHolder
            .playSpeed
            .onEach {
                speedState.value = it
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            coRunCatching {
                releaseInteractor.loadWithFranchises(argExtra.releaseId)
            }.onSuccess { releases ->
                playerController.data.value = releases
                currentReleases = releases
                currentEpisodes.clear()
                currentEpisodes.addAll(releases.flatMap { it.episodes.reversed() })
                val episodeId = currentEpisode?.id ?: argExtra.episodeId
                val episode = currentEpisodes
                    .firstOrNull { it.id == episodeId }
                    ?: currentEpisodes.firstOrNull()
                episode?.also { playEpisode(it, restart = argExtra.restart) }
            }.onFailure {

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.reset()
    }

    private fun getCurrentRelease(): Release? {
        val episodeId = currentEpisode?.id ?: return null
        return currentReleases?.find { it.id == episodeId.releaseId }
    }

    fun onPauseClick(position: Long) {
        saveEpisode(position)
    }

    fun onNextClick(position: Long) {
        getNextEpisode()?.also {
            saveEpisode(position)
            playEpisode(it)
        }
    }

    fun onPrevClick(position: Long) {
        getPrevEpisode()?.also {
            saveEpisode(position)
            playEpisode(it)
        }
    }

    fun onEpisodesClick(position: Long) {
        val release = getCurrentRelease() ?: return
        val episode = currentEpisode ?: return
        saveEpisode(position)
        guidedRouter.open(PlayerEpisodesGuidedScreen(release.id, episode.id))
    }


    fun onQualityClick(position: Long) {
        val release = getCurrentRelease() ?: return
        val episode = currentEpisode ?: return
        saveEpisode(position)
        guidedRouter.open(PlayerQualityGuidedScreen(release.id, episode.id))
    }

    fun onSpeedClick() {
        val release = getCurrentRelease() ?: return
        val episode = currentEpisode ?: return
        guidedRouter.open(PlayerSpeedGuidedScreen(release.id, episode.id))
    }

    fun onComplete(position: Long) {
        val release = getCurrentRelease() ?: return
        val episode = currentEpisode ?: return
        if (currentComplete == true) return
        currentComplete = true

        saveEpisode(position, ended = true)
        val nextEpisode = getNextEpisode()
        if (nextEpisode != null) {
            playEpisode(nextEpisode)
        } else {
            guidedRouter.open(PlayerEndSeasonGuidedScreen(release.id, episode.id))
        }
    }

    fun onPrepare(duration: Long) {
        if (duration <= 0L) return
        currentDuration = duration
        val release = getCurrentRelease() ?: return
        val episode = currentEpisode ?: return
        viewModelScope.launch {
            val access = releaseInteractor.getAccess(episode.id)
            val complete = access != null && access.seek >= duration
            if (currentComplete == complete) return@launch
            currentComplete = complete
            if (complete) {
                playAction.emit(false)
                val nextEpisode = getNextEpisode()
                if (nextEpisode == null) {
                    guidedRouter.open(PlayerEndSeasonGuidedScreen(release.id, episode.id))
                } else {
                    guidedRouter.open(PlayerEndEpisodeGuidedScreen(release.id, episode.id))
                }
            } else {
                playAction.emit(true)
            }
        }
    }

    private fun getNextEpisode(): Episode? =
        currentEpisodes.getOrNull(getCurrentEpisodeIndex() + 1)

    private fun getPrevEpisode(): Episode? =
        currentEpisodes.getOrNull(getCurrentEpisodeIndex() - 1)

    private fun getCurrentEpisodeIndex(): Int =
        currentEpisodes.indexOfFirst { it.id == currentEpisode?.id }

    private fun saveEpisode(position: Long, ended: Boolean = false) {
        val episode = currentEpisode ?: return
        if (!episodeReady || videoData.value?.episodeId != episode.id || position < 0L ||
            (currentDuration <= 0L && !ended)
        ) {
            return
        }
        val viewed = EpisodeWatchPolicy.isViewed(position, currentDuration, episode.skips?.ending?.start, ended)
        watchThresholdReached = viewed
        lastProgressSavedAt = System.currentTimeMillis()
        viewModelScope.launch {
            releaseInteractor.setPlaybackProgress(episode.id, position, viewed)
        }
    }

    private fun playEpisode(episode: Episode, force: Boolean = false, restart: Boolean = false) {
        episodeJob?.cancel()
        videoJob?.cancel()
        episodeReady = false
        currentEpisode = episode
        currentComplete = null
        currentDuration = 0L
        lastProgressSavedAt = 0L
        watchThresholdReached = false
        episodeJob = viewModelScope.launch {
            releaseInteractor.startPlayback(episode.id, restart)
            episodeReady = true
            updateQuality()
            updateEpisode(force)
            historyRepository.putReleaseId(episode.id.releaseId)
        }
    }

    private fun updateQuality() {
        val quality = currentQuality ?: return
        qualityState.value = currentEpisode?.qualityInfo?.getActualFor(quality) ?: quality
    }

    private fun updateEpisode(force: Boolean = false) {
        if (!episodeReady) return
        val release = getCurrentRelease() ?: return
        val episode = currentEpisode ?: return
        val quality = currentQuality ?: return
        videoJob?.cancel()
        videoJob = viewModelScope.launch {
            val newUrl = episode.qualityInfo.getSafeUrlFor(quality)
            val access = releaseInteractor.getAccess(episode.id)
            val newVideo = Video(
                url = newUrl,
                seek = access?.seek ?: 0,
                title = release.title.orEmpty(),
                subtitle = episode.title.orEmpty(),
                skips = episode.skips,
                episodeId = episode.id,
                playbackId = videoGeneration + 1,
            )
            if (force || videoData.value?.url != newVideo.url || videoData.value?.episodeId != episode.id) {
                videoGeneration++
                videoData.value = newVideo
            }
        }
    }
}
