package ru.radiationx.anilibria.screen.config

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_config.*
import ru.radiationx.anilibria.common.MotionLayoutListener
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.screen.BaseFragment
import ru.radiationx.shared_app.di.viewModel

class ConfigFragment : BaseFragment(R.layout.fragment_config) {

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
        configActionRepeat.setOnClickListener { viewModel.repeatCheck() }
        configActionSkip.setOnClickListener { viewModel.skipCheck() }
        configActionNext.setOnClickListener { viewModel.nextCheck() }

        mainConstraint.post {
            mainConstraint?.transitionToEnd()
            mainConstraint.setTransitionListener(startTransitionListener)
        }
        subscribeTo(viewModel.screenStateData, ::updateScreen)
        subscribeTo(viewModel.completeEvent) { startCompleteTransition() }
    }

    private fun updateScreen(screenState: ConfigScreenState) {
        Log.e("lalala", "updateScreen $screenState")
        configErrorText.text = screenState.status
        TransitionManager.beginDelayedTransition(mainConstraint)
        configProgressBar.isVisible = !screenState.needRefresh
        configErrorGroup.isVisible = screenState.needRefresh
        configActionNext.isVisible = screenState.needRefresh && screenState.hasNext
        configActionRepeat.requestFocus()
        configActionRepeat.post {
            configActionRepeat?.requestFocus()
        }
    }

    private fun startCompleteTransition() {
        mainConstraint.post {
            mainConstraint?.transitionToState(R.id.logo_end)
            mainConstraint?.setTransitionListener(completeTransitionListener)
        }
    }
}