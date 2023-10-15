package ru.radiationx.anilibria.screen.player.end_season

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment
import ru.radiationx.quill.viewModel

class EndSeasonGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
        private const val REPLAY_EPISODE_ACTION_ID = 0L
        private const val REPLAY_SEASON_ACTION_ID = 1L
        private const val CLOSE_ACTION_ID = 2L
    }

    private val viewModel by viewModel<EndSeasonViewModel> { argExtra }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(REPLAY_EPISODE_ACTION_ID)
                .title("Начать серию заново")
                .build()
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(REPLAY_SEASON_ACTION_ID)
                .title("Начать с первой серии")
                .build()
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(CLOSE_ACTION_ID)
                .title("Закрыть плеер")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        when (action.id) {
            REPLAY_EPISODE_ACTION_ID -> viewModel.onReplayEpisodeClick()
            REPLAY_SEASON_ACTION_ID -> viewModel.onReplaySeasonClick()
            CLOSE_ACTION_ID -> viewModel.onCloseClick()
        }
    }
}