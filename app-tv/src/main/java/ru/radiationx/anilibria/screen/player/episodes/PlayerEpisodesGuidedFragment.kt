package ru.radiationx.anilibria.screen.player.episodes

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class PlayerEpisodesGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
    }

    private val viewModel by viewModel<PlayerEpisodesViewModel> { argExtra }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.episodesData) {
            val selectedId = actions.getOrNull(selectedActionPosition)?.id
            actions = createGroupedActions(it)
            selectedId?.let { id ->
                actions.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { position ->
                    selectedActionPosition = position
                }
            }
        }

        subscribeTo(viewModel.selectedAction.filterNotNull()) { action ->
            selectedActionPosition = findActionPositionById(action.id)
        }
    }

    private fun createGroupedActions(groups: List<PlayerEpisodesViewModel.Group>): List<GuidedAction> {
        if (groups.size <= 1) {
            return groups.getOrNull(0)?.let { createEpisodesActions(it.actions) }.orEmpty()
        }
        return buildList {
            groups.forEach { group ->
                val groupAction = GuidedAction.Builder(requireContext())
                    .id(group.id)
                    .title(group.title)
                    .multilineDescription(true)
                    .infoOnly(true)
                    .enabled(false)
                    .focusable(false)
                    .build()
                add(groupAction)
                addAll(createEpisodesActions(group.actions))
            }
        }
    }

    private fun createEpisodesActions(
        episodes: List<PlayerEpisodesViewModel.Action>,
    ): List<GuidedAction> {
        return episodes.map { action ->
            GuidedAction.Builder(requireContext())
                .id(action.id)
                .title(action.title)
                .description(action.description)
                .apply { if (action.isViewed) icon(R.drawable.ic_episode_viewed) }
                .build()
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (!action.hasSubActions()) {
            viewModel.applyEpisode(action.id)
        }
    }

    override fun onSubGuidedActionClicked(action: GuidedAction): Boolean {
        viewModel.applyEpisode(action.id)
        return super.onSubGuidedActionClicked(action)
    }
}
