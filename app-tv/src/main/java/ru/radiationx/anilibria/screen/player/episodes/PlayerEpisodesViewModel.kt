package ru.radiationx.anilibria.screen.player.episodes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.interactors.ReleaseInteractor
import javax.inject.Inject

class PlayerEpisodesViewModel @Inject constructor(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    val episodesData = MutableStateFlow<List<Group>>(emptyList())
    val selectedAction = MutableStateFlow<Action?>(null)
    private var selectionInitialized = false

    init {
        val playerData = playerController.data.value
        val releases = playerData?.let { flowOf(it) }
            ?: releaseInteractor.observeFull(argExtra.releaseId).map { listOf(it) }
        releases.flatMapLatest { items ->
            combine(items.map { releaseInteractor.observeAccesses(it.id) }) { accesses ->
                items to accesses.flatMap { it }.associateBy { it.id }
            }
        }.onEach { (items, accesses) ->
            val groups = items.toGroups(accesses)
            episodesData.value = groups
            if (!selectionInitialized) {
                selectedAction.value = groups.findAction { it.episodeId == argExtra.episodeId }
                selectionInitialized = true
            }
        }.launchIn(viewModelScope)
    }

    fun applyEpisode(actionId: Long) {
        guidedRouter.close()
        val action = episodesData.value.findAction { it.id == actionId }
        if (action != null) {
            playerController.selectEpisodeRelay.emit(action.episodeId)
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
                Action(
                    id = id++,
                    episodeId = episode.id,
                    title = episode.title.orEmpty(),
                    description = access.progressDescription(),
                    isViewed = access?.isViewed == true,
                )
            }
            Group(
                id = groupId,
                title = release.title.orEmpty(),
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
        val isViewed: Boolean,
    )
}
