package ru.radiationx.anilibria.screen.player.end_season

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class EndSeasonViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
    private val router: Router
) : LifecycleViewModel() {


    private val currentEpisodes = mutableListOf<Episode>()
    private val currentEpisode
        get() = currentEpisodes.firstOrNull { it.id == argExtra.episodeId }

    override fun onColdCreate() {
        super.onColdCreate()
        viewModelScope.launch {
            releaseInteractor.getFull(argExtra.releaseId)?.also {
                currentEpisodes.clear()
                currentEpisodes.addAll(it.episodes.reversed())
            }
        }
    }

    fun onReplayEpisodeClick() {
        val episode = currentEpisode ?: return

        guidedRouter.close()
        releaseInteractor.putEpisode(
            episode.access.copy(
                seek = 0,
                lastAccess = System.currentTimeMillis(),
                isViewed = true
            )
        )
        playerController.selectEpisodeRelay.emit(episode.id)
    }

    fun onReplaySeasonClick() {
        guidedRouter.close()
        currentEpisodes.firstOrNull()?.also { firstEpisode ->
            playerController.selectEpisodeRelay.emit(firstEpisode.id)
        }
    }

    fun onCloseClick() {
        guidedRouter.finishGuidedChain()
        router.exit()
    }

}