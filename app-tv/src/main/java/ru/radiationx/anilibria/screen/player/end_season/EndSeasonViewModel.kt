package ru.radiationx.anilibria.screen.player.end_season

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.entity.app.release.Episode
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

    private val currentEpisodes = mutableListOf<Episode>()
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

        viewModelScope.launch {
            releaseInteractor.putEpisode(
                episode.access.copy(
                    seek = 0,
                    lastAccess = System.currentTimeMillis(),
                    isViewed = true
                )
            )
            playerController.selectEpisodeRelay.emit(episode.id)
            guidedRouter.close()
        }
    }

    fun onReplaySeasonClick() {
        viewModelScope.launch {
            currentEpisodes.firstOrNull()?.also { firstEpisode ->
                playerController.selectEpisodeRelay.emit(firstEpisode.id)
            }
            guidedRouter.close()
        }
    }

    fun onCloseClick() {
        guidedRouter.finishGuidedChain()
        router.exit()
    }

}