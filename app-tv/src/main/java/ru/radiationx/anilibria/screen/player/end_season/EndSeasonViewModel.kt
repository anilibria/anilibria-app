package ru.radiationx.anilibria.screen.player.end_season

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.interactors.ReleaseInteractor
import javax.inject.Inject

class EndSeasonViewModel @Inject constructor(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
    private val router: Router,
) : LifecycleViewModel() {

    private val currentEpisodes = mutableListOf<Episode>()
    private val currentEpisode
        get() = currentEpisodes.firstOrNull { it.id == argExtra.episodeId }

    init {
        viewModelScope.launch {
            releaseInteractor.getFull(argExtra.releaseId)?.also {
                currentEpisodes.clear()
                currentEpisodes.addAll(it.episodes.reversed())
            }
        }
    }

    fun onReplayEpisodeClick() {
        val episode = currentEpisode ?: return
        viewModelScope.launch {
            releaseInteractor.startPlayback(episode.id, restart = true)
            playerController.selectEpisodeRelay.emit(episode.id)
            guidedRouter.close()
        }
    }

    fun onReplaySeasonClick() {
        val firstEpisode = currentEpisodes.firstOrNull() ?: return
        viewModelScope.launch {
            releaseInteractor.startPlayback(firstEpisode.id, restart = true)
            playerController.selectEpisodeRelay.emit(firstEpisode.id)
            guidedRouter.close()
        }
    }

    fun onCloseClick() {
        guidedRouter.finishGuidedChain()
        router.exit()
    }

}
