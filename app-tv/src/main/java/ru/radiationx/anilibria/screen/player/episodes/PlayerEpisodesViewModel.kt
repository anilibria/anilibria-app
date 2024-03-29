package ru.radiationx.anilibria.screen.player.episodes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.shared.ktx.asTimeSecString
import toothpick.InjectConstructor
import java.util.Date

@InjectConstructor
class PlayerEpisodesViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    val episodesData = MutableStateFlow<List<Pair<String, String?>>>(emptyList())
    val selectedIndex = MutableStateFlow<Int?>(null)

    private val currentEpisodes = mutableListOf<Episode>()

    init {
        releaseInteractor
            .observeFull(argExtra.releaseId)
            .onEach {
                updateEpisodes(it)
            }
            .launchIn(viewModelScope)
    }

    fun applyEpisode(index: Int) {
        guidedRouter.close()
        playerController.selectEpisodeRelay.emit(currentEpisodes[index].id)
    }

    private fun updateEpisodes(release: Release) {
        viewModelScope.launch {
            currentEpisodes.clear()
            currentEpisodes.addAll(release.episodes.reversed())
            val accesses = releaseInteractor.getAccesses(release.id).associateBy { it.id }
            episodesData.value = currentEpisodes.map {
                val access = accesses[it.id]
                val description = if (access != null && access.isViewed && access.seek > 0) {
                    "Остановлена на ${Date(access.seek).asTimeSecString()}"
                } else {
                    null
                }
                Pair(it.title.orEmpty(), description)
            }
            selectedIndex.value = currentEpisodes.indexOfLast { it.id == argExtra.episodeId }
        }

    }
}