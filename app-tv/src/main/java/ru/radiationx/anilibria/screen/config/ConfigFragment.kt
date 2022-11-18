package ru.radiationx.anilibria.screen.config

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.MotionLayoutListener
import ru.radiationx.anilibria.databinding.FragmentConfigBinding
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import ru.radiationx.shared_app.screen.ScopedFragment

class ConfigFragment : ScopedFragment(R.layout.fragment_config) {

    private val binding by viewBinding<FragmentConfigBinding>()

    private val viewModel: ConfiguringViewModel by viewModel()

    private val startTransitionListener = object : MotionLayoutListener() {
        override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
            when (currentId) {
                R.id.logo_end -> motionLayout.transitionToState(R.id.logo_end_progress)
                R.id.logo_end_progress -> viewModel.startConfiguring()
            }
        }
    }

    private val completeTransitionListener = object : MotionLayoutListener() {
        override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
            if (currentId == R.id.logo_end) {
                viewModel.endConfiguring()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configActionRepeat.setOnClickListener { viewModel.repeatCheck() }
        binding.configActionSkip.setOnClickListener { viewModel.skipCheck() }
        binding.configActionNext.setOnClickListener { viewModel.nextCheck() }

        binding.mainConstraint.post {
            binding.mainConstraint.transitionToEnd()
            binding.mainConstraint.setTransitionListener(startTransitionListener)
        }
        subscribeTo(viewModel.screenStateData, ::updateScreen)
        subscribeTo(viewModel.completeEvent) { startCompleteTransition() }
    }

    private fun updateScreen(screenState: ConfigScreenState) {
        binding.configErrorText.text = screenState.status
        binding.configActionNext.setText(
            if (screenState.hasNext) {
                R.string.config_action_next
            } else {
                R.string.config_action_restart
            }
        )

        TransitionManager.beginDelayedTransition(binding.mainConstraint)
        binding.configProgressBar.isVisible = !screenState.needRefresh
        binding.configErrorGroup.isVisible = screenState.needRefresh
        binding.configActionNext.isVisible = screenState.needRefresh
        binding.configActionRepeat.requestFocus()
        binding.configActionRepeat.post {
            binding.configActionRepeat.requestFocus()
        }
    }

    private fun startCompleteTransition() {
        binding.mainConstraint.post {
            binding.mainConstraint.transitionToState(R.id.logo_end)
            binding.mainConstraint.setTransitionListener(completeTransitionListener)
        }
    }
}