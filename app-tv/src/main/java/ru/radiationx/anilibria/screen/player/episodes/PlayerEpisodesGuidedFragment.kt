package ru.radiationx.anilibria.screen.player.episodes

import android.os.Bundle
import android.util.Log
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

        private const val CHUNK_SIZE = 20
        private const val CHUNK_THRESHOLD = 32
        private const val CHUNK_ID_OFFSET = 100000
        private const val CHUNK_ENABLED = false

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
            actions = if (CHUNK_ENABLED && it.size > CHUNK_THRESHOLD) {
                createChunkedActions(it)
            } else {
                createEpisodesActions(0, it)
            }
        }

        subscribeTo(viewModel.selectedIndex) { selectedIndex ->
            if (actions.any { it.hasSubActions() }) {
                val chunkActionId = ((selectedIndex / CHUNK_SIZE) + CHUNK_ID_OFFSET).toLong()
                val chunkPosition = findActionPositionById(chunkActionId)
                expandAction(findActionById(chunkActionId), false)
                selectedActionPosition = chunkPosition
            } else {
                selectedActionPosition = selectedIndex
            }
        }
    }

    private fun createChunkedActions(episodes: List<Pair<String, String?>>): List<GuidedAction> =
        episodes.chunked(CHUNK_SIZE).mapIndexed { index: Int, chunk: List<Pair<String, String?>> ->
            val first = chunk.first().first
            val last = chunk.last().first
            val offset = index * CHUNK_SIZE
            GuidedAction.Builder(requireContext())
                .id((CHUNK_ID_OFFSET + index).toLong())
                .title("$first – $last")
                .subActions(createEpisodesActions(offset, chunk))
                .build()
        }

    private fun createEpisodesActions(offset: Int, episodes: List<Pair<String, String?>>): List<GuidedAction> =
        episodes.mapIndexed { index: Int, data: Pair<String, String?> ->
            GuidedAction.Builder(requireContext())
                .id((offset + index).toLong())
                .title(data.first)
                .description(data.second)
                .build()
        }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (!action.hasSubActions()) {
            viewModel.applyEpisode(action.id.toInt())
        }
    }

    override fun onSubGuidedActionClicked(action: GuidedAction): Boolean {
        viewModel.applyEpisode(action.id.toInt())
        return super.onSubGuidedActionClicked(action)
    }
}