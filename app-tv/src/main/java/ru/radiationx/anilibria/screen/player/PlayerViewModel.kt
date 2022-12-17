package ru.radiationx.anilibria.screen.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.*
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController
) : LifecycleViewModel() {

    val videoData = MutableLiveData<Video>()
    val qualityState = MutableLiveData<Int>()
    val speedState = MutableLiveData<Float>()
    val playAction = MutableLiveData<Boolean>()

    private var currentEpisodes = mutableListOf<Episode>()
    private var currentRelease: Release? = null
    private var currentEpisode: Episode? = null
    private var currentQuality: Int? = null
    private var currentComplete: Boolean? = null

    init {
        qualityState.value = releaseInteractor.getQuality()
        speedState.value = releaseInteractor.getPlaySpeed()

        playerController
            .selectEpisodeRelay
            .onEach { episodeId ->
                currentEpisodes
                    .firstOrNull { it.id == episodeId }
                    ?.also { playEpisode(it, true) }
            }
            .launchIn(viewModelScope)

        releaseInteractor
            .observeQuality()
            .distinctUntilChanged()
            .onEach {
                currentQuality = handleRawQuality(it)
                updateQuality()
                updateEpisode()
            }
            .launchIn(viewModelScope)

        releaseInteractor
            .observePlaySpeed()
            .distinctUntilChanged()
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

    fun onPlayClick(position: Long) {

    }

    fun onPauseClick(position: Long) {
        saveEpisode(position)
    }

    fun onReplayClick(position: Long) {

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

    fun onSpeedClick(position: Long) {
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

    fun onPrepare(position: Long, duration: Long) {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        val complete = episode.access.seek >= duration
        if (currentComplete == complete) return
        currentComplete = complete
        if (complete) {
            playAction.value = false
            val nextEpisode = getNextEpisode()
            if (nextEpisode == null) {
                guidedRouter.open(PlayerEndSeasonGuidedScreen(release.id, episode.id))
            } else {
                guidedRouter.open(PlayerEndEpisodeGuidedScreen(release.id, episode.id))
            }
        } else {
            playAction.value = true
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
        releaseInteractor.putEpisode(
            episode.access.copy(
                seek = position,
                lastAccess = System.currentTimeMillis(),
                isViewed = true
            )
        )
    }

    private fun playEpisode(episode: Episode, force: Boolean = false) {
        currentEpisode = episode
        currentComplete = null
        updateQuality()
        updateEpisode(force)
    }

    private fun updateQuality() {
        val quality = currentQuality ?: return
        qualityState.value = currentEpisode?.let { getEpisodeQuality(it, quality) } ?: quality
    }

    private fun updateEpisode(force: Boolean = false) {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        val quality = currentQuality ?: return

        val newVideo = episode.let {
            val url = getEpisodeUrl(it, quality)
            Video(url!!, episode.access.seek, release.title.orEmpty(), it.title.orEmpty())
        }
        if (force || videoData.value?.url != newVideo.url) {
            videoData.value = newVideo
        }
    }

    private fun handleRawQuality(quality: Int): Int = when (quality) {
        PreferencesHolder.QUALITY_NO,
        PreferencesHolder.QUALITY_ALWAYS -> PreferencesHolder.QUALITY_SD
        else -> quality
    }

    private fun getEpisodeQuality(episode: Episode, quality: Int): Int {
        var newQuality = quality

        if (newQuality == PreferencesHolder.QUALITY_FULL_HD && episode.urlFullHd.isNullOrEmpty()) {
            newQuality = PreferencesHolder.QUALITY_HD
        }
        if (newQuality == PreferencesHolder.QUALITY_HD && episode.urlHd.isNullOrEmpty()) {
            newQuality = PreferencesHolder.QUALITY_SD
        }
        if (newQuality == PreferencesHolder.QUALITY_SD && episode.urlSd.isNullOrEmpty()) {
            newQuality = -1
        }

        return newQuality
    }

    private fun getEpisodeUrl(episode: Episode, quality: Int): String? =
        when (getEpisodeQuality(episode, quality)) {
            PreferencesHolder.QUALITY_FULL_HD -> episode.urlFullHd
            PreferencesHolder.QUALITY_HD -> episode.urlHd
            PreferencesHolder.QUALITY_SD -> episode.urlSd
            else -> null
        }
}