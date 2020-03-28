package ru.radiationx.anilibria.screen.player

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.PlayerQualityGuidedScreen
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    var argReleaseId = -1

    val videoData = MutableLiveData<Video>()
    val qualityState = MutableLiveData<Int>()
    val speedState = MutableLiveData<Float>()

    private var currentRelease: ReleaseFull? = null
    private var currentEpisode: ReleaseFull.Episode? = null
    private var currentQuality: Int? = null

    override fun onCreate() {
        super.onCreate()


        qualityState.value = releaseInteractor.getQuality()

        speedState.value = releaseInteractor.getPlaySpeed()


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
            .observeFull(argReleaseId)
            .lifeSubscribe { release ->
                currentRelease = release
                currentEpisode = release.episodes.last()
                updateQuality()
                updateEpisode()
            }
    }


    fun onSeekChanged(seek: Long) {

    }

    fun onPlayClick() {

    }

    fun onPauseClick() {

    }

    fun onReplayClick() {

    }

    fun onNextClick() {

    }

    fun onPrevClick() {

    }


    fun onEpisodesClick() {

    }

    fun onQualityClick() {
        val release = currentRelease ?: return
        val episode = currentEpisode ?: return
        guidedRouter.open(PlayerQualityGuidedScreen(release.id, episode.id))
    }

    fun onSpeedClick() {

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
            Video(url!!, 0L, release.title.orEmpty(), it.title.orEmpty())
        }
        if (videoData.value != newVideo) {
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