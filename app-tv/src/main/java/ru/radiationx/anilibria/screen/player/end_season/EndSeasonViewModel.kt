package ru.radiationx.anilibria.screen.player.end_season

import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class EndSeasonViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
    private val router: Router
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

    fun onReplayEpisodeClick() {
        val episode = currentEpisode ?: return

        releaseInteractor.putEpisode(episode.apply {
            seek = 0
            lastAccess = System.currentTimeMillis()
            isViewed = true
        })
        playerController.selectEpisodeRelay.accept(episode.id)
        guidedRouter.close()
    }

    fun onReplaySeasonClick() {
        currentEpisodes.firstOrNull()?.also { firstEpisode ->
            playerController.selectEpisodeRelay.accept(firstEpisode.id)
        }
        guidedRouter.close()
    }

    fun onCloseClick() {
        guidedRouter.finishGuidedChain()
        router.exit()
    }

}