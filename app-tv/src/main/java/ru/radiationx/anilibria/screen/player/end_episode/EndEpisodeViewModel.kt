package ru.radiationx.anilibria.screen.player.end_episode

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class EndEpisodeViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    private val currentEpisodes = mutableListOf<Episode>()
    private val currentEpisode
        get() = currentEpisodes.firstOrNull { it.id == argExtra.episodeId }

    init {
        releaseInteractor
            .observeFull(argExtra.releaseId)
            .onEach {
                currentEpisodes.clear()
                currentEpisodes.addAll(it.episodes)
            }
            .launchIn(viewModelScope)
    }

    fun onReplayClick() {
        val episode = currentEpisode ?: return
        viewModelScope.launch {
            releaseInteractor.setAccessSeek(episode.id, 0)
            playerController.selectEpisodeRelay.emit(episode.id)
            guidedRouter.close()
        }
    }

    fun onNextClick() {
        val episode = currentEpisode ?: return
        guidedRouter.close()

        val currentIndex = currentEpisodes.indexOfFirst { it.id == episode.id }
        currentEpisodes.getOrNull(currentIndex + 1)?.also { nextEpisode ->
            playerController.selectEpisodeRelay.emit(nextEpisode.id)
        }
    }
}