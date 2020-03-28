package ru.radiationx.anilibria.screen.player

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.PlayerEpisodesGuidedScreen
import ru.radiationx.anilibria.screen.PlayerQualityGuidedScreen
import ru.radiationx.anilibria.screen.PlayerSpeedGuidedScreen
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController
) : LifecycleViewModel() {

    var argReleaseId = -1
    var argEpisodeId = -1

    val videoData = MutableLiveData<Video>()
    val qualityState = MutableLiveData<Int>()
    val speedState = MutableLiveData<Float>()

    private var currentEpisodes = mutableListOf<ReleaseFull.Episode>()
    private var currentRelease: ReleaseFull? = null
    private var currentEpisode: ReleaseFull.Episode? = null
    private var currentQuality: Int? = null

    override fun onCreate() {
        super.onCreate()

        qualityState.value = releaseInteractor.getQuality()
        speedState.value = releaseInteractor.getPlaySpeed()

        playerController
            .selectEpisodeRelay
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe { episodeId ->
                currentEpisode = currentRelease?.episodes?.firstOrNull { it.id == episodeId }
                updateEpisode()
                updateQuality()
            }

        releaseInteractor
            .observeQuality()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                currentQuality = handleRawQuality(it)
                updateQuality()
                updateEpisode()
            }

        releaseInteractor
            .observePlaySpeed()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                speedState.value = it
            }

        releaseInteractor
            .observeFull(argReleaseId)
            .lifeSubscribe { release ->
                currentRelease = release
                currentEpisodes.clear()
                currentEpisodes.addAll(release.episodes.reversed())
                val episodeId = currentEpisode?.id ?: argEpisodeId
                currentEpisode = currentEpisodes.firstOrNull { it.id == episodeId } ?: currentEpisodes.firstOrNull()
                updateQuality()
                updateEpisode()
            }
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
            currentEpisode = it
            updateQuality()
            updateEpisode()
        }
    }

    fun onPrevClick(position: Long) {
        getPrevEpisode()?.also {
            saveEpisode(position)
            currentEpisode = it
            updateQuality()
            updateEpisode()
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
        guidedRouter.open(PlayerSpeedGuidedScreen())
    }

    private fun getNextEpisode(): ReleaseFull.Episode? = currentEpisodes.getOrNull(getCurrentEpisodeIndex() + 1)

    private fun getPrevEpisode(): ReleaseFull.Episode? = currentEpisodes.getOrNull(getCurrentEpisodeIndex() - 1)

    private fun getCurrentEpisodeIndex(): Int = currentEpisodes.indexOfFirst { it.id == currentEpisode?.id }

    private fun saveEpisode(position: Long) {
        val episode = currentEpisode ?: return
        if (position < 0) {
            return
        }
        releaseInteractor.putEpisode(episode.apply {
            seek = position
            lastAccess = System.currentTimeMillis()
            isViewed = true
        })
    }

    private fun updateQuality() {
        val quality = currentQuality ?: return
        qualityState.value = currentEpisode?.let { getEpisodeQuality(it, quality) } ?: quality
    }

    private fun updateEpisode() {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        val quality = currentQuality ?: return

        val newVideo = episode.let {
            val url = getEpisodeUrl(it, quality)
            Video(url!!, episode.seek, release.title.orEmpty(), it.title.orEmpty())
        }
        if (videoData.value?.url != newVideo.url) {
            videoData.value = newVideo
        }
    }

    private fun handleRawQuality(quality: Int): Int = when (quality) {
        PreferencesHolder.QUALITY_NO,
        PreferencesHolder.QUALITY_ALWAYS -> PreferencesHolder.QUALITY_SD
        else -> quality
    }

    private fun getEpisodeQuality(episode: ReleaseFull.Episode, quality: Int): Int {
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

    private fun getEpisodeUrl(episode: ReleaseFull.Episode, quality: Int): String? = when (getEpisodeQuality(episode, quality)) {
        PreferencesHolder.QUALITY_FULL_HD -> episode.urlFullHd
        PreferencesHolder.QUALITY_HD -> episode.urlHd
        PreferencesHolder.QUALITY_SD -> episode.urlSd
        else -> null
    }
}