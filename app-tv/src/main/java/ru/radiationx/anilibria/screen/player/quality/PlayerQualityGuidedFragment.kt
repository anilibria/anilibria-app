package ru.radiationx.anilibria.screen.player.quality

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class PlayerQualityGuidedFragment : BasePlayerGuidedFragment() {

    private val viewModel by quillViewModel<PlayerQualityViewModel>()

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

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        viewModel.argReleaseId = releaseId
        viewModel.argEpisodeId = episodeId
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