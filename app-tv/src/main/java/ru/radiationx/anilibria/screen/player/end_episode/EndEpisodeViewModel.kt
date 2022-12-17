package ru.radiationx.anilibria.screen.player.end_episode

import androidx.lifecycle.viewModelScope
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
    private val playerController: PlayerController
) : LifecycleViewModel() {

    private val currentEpisodes = mutableListOf<Episode>()
    private val currentEpisode
        get() = currentEpisodes.firstOrNull { it.id == argExtra.episodeId }

    override fun onCreate() {
        super.onCreate()
        releaseInteractor.getFull(argExtra.releaseId)?.also {
            currentEpisodes.clear()
            currentEpisodes.addAll(it.episodes.reversed())
        }
    }

    fun onReplayClick() {
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

    fun onNextClick() {
        val episode = currentEpisode ?: return
        viewModelScope.launch {
            val currentIndex = currentEpisodes.indexOfFirst { it.id == episode.id }

            currentEpisodes.getOrNull(currentIndex + 1)?.also { nextEpisode ->
                playerController.selectEpisodeRelay.emit(nextEpisode.id)
            }
            guidedRouter.close()
        }
    }
}