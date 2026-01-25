package ru.radiationx.anilibria.screen.player.speed

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class PlayerSpeedGuidedFragment : BasePlayerGuidedFragment() {

    private val viewModel by viewModel<PlayerSpeedViewModel>()

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.speedState) {
            actions = it.speeds.mapIndexed { index: Int, speed: Float ->
                GuidedAction.Builder(requireContext())
                    .id(index.toLong())
                    .title(speed.toTitle())
                    .build()
            }
            selectedActionPosition = it.selectedIndex ?: -1
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.applySpeed(action.id.toInt())
    }

    private fun Float.toTitle(): String {
        return if (this == 1.0f) {
            "Нормальная"
        } else {
            "${this}x"
        }
    }
}