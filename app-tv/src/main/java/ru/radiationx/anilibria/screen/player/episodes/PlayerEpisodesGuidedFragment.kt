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
            actions = createGroupedActions(it)
        }

        subscribeTo(viewModel.selectedAction.filterNotNull()) { action ->
            selectedActionPosition = findActionPositionById(action.id)
        }
    }

    private fun createGroupedActions(groups: List<PlayerEpisodesViewModel.Group>): List<GuidedAction> {
        return groups.map { group ->
            GuidedAction.Builder(requireContext())
                .id(group.id)
                .title(group.title)
                .subActions(createEpisodesActions(group.actions))
                .build()
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