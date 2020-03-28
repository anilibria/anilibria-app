package ru.radiationx.anilibria.screen.player.quality

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

class PlayerQualityGuidedFragment : ScopedGuidedStepFragment() {

    companion object {
        private const val ARG_RELEASE_ID = "release id"
        private const val ARG_EPISODE_ID = "episode id"

        fun newInstance(releaseId: Int = -1, episodeId: Int = -1) = PlayerQualityGuidedFragment().putExtra {
            putInt(ARG_RELEASE_ID, releaseId)
            putInt(ARG_EPISODE_ID, episodeId)
        }
    }

    private val viewModel by viewModel<PlayerQualityViewModel>()

    private val sdAction by lazy {
        GuidedAction.Builder(requireContext())
            .id(PlayerQualityViewModel.SD_ACTION_ID)
            .title("480p")
            .icon(requireContext().getCompatDrawable(R.drawable.ic_quality_sd_base))
            .build()
    }

    private val hdAction by lazy {
        GuidedAction.Builder(requireContext())
            .id(PlayerQualityViewModel.HD_ACTION_ID)
            .title("720p")
            .icon(requireContext().getCompatDrawable(R.drawable.ic_quality_hd_base))
            .build()
    }

    private val fullHdAction by lazy {
        GuidedAction.Builder(requireContext())
            .id(PlayerQualityViewModel.FULL_HD_ACTION_ID)
            .title("1080p")
            .icon(requireContext().getCompatDrawable(R.drawable.ic_quality_full_hd_base))
            .build()
    }

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

        subscribeTo(viewModel.availableData) {
            actions = it.mapNotNull { id -> getActionById(id) }
        }

        subscribeTo(viewModel.selectedData) {
            selectedActionPosition = findActionPositionById(it)
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.applyQuality(action.id)
    }

    private fun getActionById(id: Long): GuidedAction? = when (id) {
        PlayerQualityViewModel.SD_ACTION_ID -> sdAction
        PlayerQualityViewModel.HD_ACTION_ID -> hdAction
        PlayerQualityViewModel.FULL_HD_ACTION_ID -> fullHdAction
        else -> null
    }
}