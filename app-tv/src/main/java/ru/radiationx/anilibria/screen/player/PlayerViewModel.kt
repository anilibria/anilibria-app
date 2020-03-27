package ru.radiationx.anilibria.screen.player

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerViewModel(
    private val releaseInteractor: ReleaseInteractor
) : LifecycleViewModel() {

    var argReleaseId = -1

    val videoData = MutableLiveData<Video>()

    override fun onCreate() {
        super.onCreate()

        releaseInteractor
            .observeFull(argReleaseId)
            .lifeSubscribe { release ->
                videoData.value = release.episodes.first().let {
                    Video(it.urlHd!!, 0L, release.title.orEmpty(), it.title.orEmpty())
                }
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

    }

    fun onSpeedClick() {

    }
}