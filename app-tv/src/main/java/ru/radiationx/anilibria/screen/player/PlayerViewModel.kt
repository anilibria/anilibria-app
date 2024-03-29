package ru.radiationx.anilibria.screen.player

import androidx.lifecycle.viewModelScope
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
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.shared.ktx.EventFlow
import toothpick.InjectConstructor

@InjectConstructor
class PlayerViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val preferencesHolder: PreferencesHolder,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    val videoData = MutableStateFlow<Video?>(null)
    val qualityState = MutableStateFlow<PlayerQuality?>(null)
    val speedState = MutableStateFlow<Float?>(null)
    val playAction = EventFlow<Boolean>()

    private var currentEpisodes = mutableListOf<Episode>()
    private var currentRelease: Release? = null
    private var currentEpisode: Episode? = null
    private var currentQuality: PlayerQuality? = null
    private var currentComplete: Boolean? = null

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

        releaseInteractor
            .observeFull(argExtra.releaseId)
            .onEach { release ->
                currentRelease = release
                currentEpisodes.clear()
                currentEpisodes.addAll(release.episodes.reversed())
                val episodeId = currentEpisode?.id ?: argExtra.episodeId
                val episode = currentEpisodes.firstOrNull { it.id == episodeId }
                    ?: currentEpisodes.firstOrNull()
                episode?.also { playEpisode(it) }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        playerController.reset()
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
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        saveEpisode(position)
        guidedRouter.open(PlayerEpisodesGuidedScreen(release.id, episode.id))
    }


    fun onQualityClick(position: Long) {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        saveEpisode(position)
        guidedRouter.open(PlayerQualityGuidedScreen(release.id, episode.id))
    }

    fun onSpeedClick() {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        guidedRouter.open(PlayerSpeedGuidedScreen(release.id, episode.id))
    }

    fun onComplete(position: Long) {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        if (currentComplete == true) return
        currentComplete = true

        saveEpisode(position)
        val nextEpisode = getNextEpisode()
        if (nextEpisode != null) {
            playEpisode(nextEpisode)
        } else {
            guidedRouter.open(PlayerEndSeasonGuidedScreen(release.id, episode.id))
        }
    }

    fun onPrepare(duration: Long) {
        val release = currentRelease ?: return
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

    private fun saveEpisode(position: Long) {
        val episode = currentEpisode ?: return
        if (position < 0) {
            return
        }
        viewModelScope.launch {
            releaseInteractor.setAccessSeek(episode.id, position)
        }
    }

    private fun playEpisode(episode: Episode, force: Boolean = false) {
        currentEpisode = episode
        currentComplete = null
        updateQuality()
        updateEpisode(force)
    }

    private fun updateQuality() {
        val quality = currentQuality ?: return
        qualityState.value = currentEpisode?.qualityInfo?.getActualFor(quality) ?: quality
    }

    private fun updateEpisode(force: Boolean = false) {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        val quality = currentQuality ?: return
        viewModelScope.launch {
            val newUrl = episode.qualityInfo.getSafeUrlFor(quality)
            val access = releaseInteractor.getAccess(episode.id)
            val newVideo = Video(
                url = newUrl,
                seek = access?.seek ?: 0,
                title = release.title.orEmpty(),
                subtitle = episode.title.orEmpty(),
                episode.skips
            )
            if (force || videoData.value?.url != newVideo.url) {
                videoData.value = newVideo
            }
        }
    }
}