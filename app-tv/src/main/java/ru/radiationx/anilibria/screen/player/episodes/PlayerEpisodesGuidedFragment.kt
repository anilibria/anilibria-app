package ru.radiationx.anilibria.screen.player.episodes

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class PlayerEpisodesGuidedFragment : BasePlayerGuidedFragment() {

    companion object {
        private const val CHUNK_SIZE = 20
        private const val CHUNK_THRESHOLD = 32
        private const val CHUNK_ID_OFFSET = 100000
        private const val CHUNK_ENABLED = false
    }

    private val viewModel by viewModel<PlayerEpisodesViewModel>()

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        viewModel.argReleaseId = releaseId
        viewModel.argEpisodeId = episodeId
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

    private fun createEpisodesActions(
        offset: Int,
        episodes: List<Pair<String, String?>>
    ): List<GuidedAction> =
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