package ru.radiationx.anilibria.screen.player.end_episode

import android.util.Log
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class EndEpisodeViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController
) : LifecycleViewModel() {

    var argReleaseId = -1
    var argEpisodeId = -1

    private val currentEpisodes = mutableListOf<ReleaseFull.Episode>()
    private val currentEpisode
        get() = currentEpisodes.firstOrNull { it.id == argEpisodeId }

    override fun onCreate() {
        super.onCreate()
        releaseInteractor.getFull(argReleaseId)?.also {
            currentEpisodes.clear()
            currentEpisodes.addAll(it.episodes.reversed())
        }
    }

    fun onReplayClick() {
        val episode = currentEpisode ?: return

        releaseInteractor.putEpisode(episode.apply {
            seek = 0
            lastAccess = System.currentTimeMillis()
            isViewed = true
        })
        playerController.selectEpisodeRelay.accept(episode.id)
        guidedRouter.close()
    }

    fun onNextClick() {
        val episode = currentEpisode ?: return
        val currentIndex = currentEpisodes.indexOfFirst { it.id == episode.id }

        currentEpisodes.getOrNull(currentIndex + 1)?.also { nextEpisode ->
            playerController.selectEpisodeRelay.accept(nextEpisode.id)
        }
        guidedRouter.close()
    }
}