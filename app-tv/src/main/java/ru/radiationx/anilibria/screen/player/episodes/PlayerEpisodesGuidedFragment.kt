package ru.radiationx.anilibria.screen.player.episodes

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.scoped.ScopedGuidedStepFragment
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import java.lang.IllegalStateException

class PlayerEpisodesGuidedFragment : ScopedGuidedStepFragment() {

    companion object {
        private const val ARG_RELEASE_ID = "release id"
        private const val ARG_EPISODE_ID = "episode id"

        fun newInstance(releaseId: Int = -1, episodeId: Int = -1) = PlayerEpisodesGuidedFragment().putExtra {
            putInt(ARG_RELEASE_ID, releaseId)
            putInt(ARG_EPISODE_ID, episodeId)
        }
    }

    private val viewModel by viewModel<PlayerEpisodesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        arguments?.apply {
            viewModel.argReleaseId = getInt(ARG_RELEASE_ID, viewModel.argReleaseId)
            viewModel.argEpisodeId = getInt(ARG_EPISODE_ID, viewModel.argEpisodeId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeTo(viewModel.episodesData) {
            actions = it.mapIndexed { index: Int, title: String ->
                GuidedAction.Builder(requireContext())
                    .id(index.toLong())
                    .title(title)
                    .build()
            }
        }

        subscribeTo(viewModel.selectedIndex) {
            selectedActionPosition = it
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.applyEpisode(action.id.toInt())
    }
}