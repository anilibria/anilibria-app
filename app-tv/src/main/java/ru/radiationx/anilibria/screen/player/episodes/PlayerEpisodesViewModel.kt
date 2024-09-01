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
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
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

    val episodesData = MutableStateFlow<List<Group>>(emptyList())
    val selectedAction = MutableStateFlow<Action?>(null)

    init {
        val playerData = playerController.data.value
        if (playerData != null) {
            updateEpisodes(playerData)
        } else {
            releaseInteractor
                .observeFull(argExtra.releaseId)
                .onEach {
                    updateEpisodes(listOf(it))
                }
                .launchIn(viewModelScope)
        }
    }

    fun applyEpisode(actionId: Long) {
        guidedRouter.close()
        val action = episodesData.value.findAction { it.id == actionId }
        if (action != null) {
            playerController.selectEpisodeRelay.emit(action.episodeId)
        }
    }

    private fun updateEpisodes(releases: List<Release>) {
        viewModelScope.launch {
            val accesses = releases
                .flatMap { releaseInteractor.getAccesses(it.id) }
                .associateBy { it.id }
            val groups = releases.toGroups(accesses)
            episodesData.value = groups
            selectedAction.value = groups.findAction { it.episodeId == argExtra.episodeId }
        }
    }

    private fun List<Group>.findAction(block: (Action) -> Boolean): Action? {
        forEach {
            val action = it.actions.find(block)
            if (action != null) {
                return action
            }
        }
        return null
    }

    private fun List<Release>.toGroups(accesses: Map<EpisodeId, EpisodeAccess>): List<Group> {
        var id = 0L
        return map { release ->
            val groupId = id++
            val actions = release.episodes.asReversed().map { episode ->
                val access = accesses[episode.id]
                val description = if (access != null && access.isViewed && access.seek > 0) {
                    "Остановлена на ${Date(access.seek).asTimeSecString()}"
                } else {
                    null
                }
                Action(
                    id = id++,
                    episodeId = episode.id,
                    title = episode.title.orEmpty(),
                    description = description
                )
            }
            Group(
                id = groupId,
                title = release.names.main,
                actions = actions
            )
        }
    }

    data class Group(
        val id: Long,
        val title: String,
        val actions: List<Action>,
    )

    data class Action(
        val id: Long,
        val episodeId: EpisodeId,
        val title: String,
        val description: String?,
    )
}