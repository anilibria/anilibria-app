package ru.radiationx.anilibria.screen.player.quality

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getCompatDrawable
import ru.radiationx.shared.ktx.android.subscribeTo

class PlayerQualityGuidedFragment : BasePlayerGuidedFragment() {

    private val viewModel by viewModel<PlayerQualityViewModel> { argExtra }

    private val autoAction by lazy {
        GuidedAction.Builder(requireContext())
            .id(PlayerQualityViewModel.AUTO_ACTION_ID)
            .title(autoTitle(null))
            .icon(requireContext().getCompatDrawable(R.drawable.ic_quality_auto_base))
            .build()
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.automaticQualityData) { quality ->
            autoAction.title = autoTitle(quality)
            findActionPositionById(PlayerQualityViewModel.AUTO_ACTION_ID)
                .takeIf { it >= 0 }
                ?.also { notifyActionChanged(it) }
        }

        subscribeTo(viewModel.availableData) {
            actions = it.mapNotNull { id -> getActionById(id) }
        }

        subscribeTo(viewModel.selectedData.filterNotNull()) {
            selectedActionPosition = findActionPositionById(it)
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.applyQuality(action.id)
    }

    private fun getActionById(id: Long): GuidedAction? = when (id) {
        PlayerQualityViewModel.AUTO_ACTION_ID -> autoAction
        PlayerQualityViewModel.SD_ACTION_ID -> sdAction
        PlayerQualityViewModel.HD_ACTION_ID -> hdAction
        PlayerQualityViewModel.FULL_HD_ACTION_ID -> fullHdAction
        else -> null
    }

    private fun autoTitle(quality: PlayerQuality?): String = when (quality) {
        PlayerQuality.SD -> "480p"
        PlayerQuality.HD -> "720p"
        PlayerQuality.FULLHD -> "1080p"
        null -> "Авто"
    }
}
