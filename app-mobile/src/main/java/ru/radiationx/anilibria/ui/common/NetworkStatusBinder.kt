package ru.radiationx.anilibria.ui.common

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import kotlinx.coroutines.delay
import ru.radiationx.shared.ktx.android.setBackgroundTintRes
import ru.radiationx.shared_app.networkstatus.NetworkStatusState
import ru.radiationx.shared_app.networkstatus.toViewState

object NetworkStatusBinder {

    suspend fun bindPlayer(
        transitionRoot: ViewGroup,
        statusWrapper: ViewGroup,
        statusView: TextView,
        state: NetworkStatusState
    ) {
        val viewState = state.toViewState()
        statusView.text = viewState.text
        statusWrapper.setBackgroundTintRes(viewState.colorRes)
        if (!viewState.isVisible) {
            delay(500)
        }
        updateVisibilityError(transitionRoot, statusWrapper, viewState.isVisible)
    }

    suspend fun bind(
        transitionRoot: ViewGroup,
        statusWrapper: ViewGroup,
        statusView: TextView,
        state: NetworkStatusState
    ) {
        val viewState = state.toViewState()
        statusView.text = viewState.text
        statusView.setBackgroundTintRes(viewState.colorRes)
        if (!viewState.isVisible) {
            delay(500)
        }
        updateVisibilityError(transitionRoot, statusWrapper, viewState.isVisible)
    }

    private fun updateVisibilityError(
        transitionRoot: ViewGroup,
        statusWrapper: ViewGroup,
        visible: Boolean
    ) {
        TransitionManager.beginDelayedTransition(
            transitionRoot,
            AutoTransition().apply {
                addTarget(statusWrapper)
            }
        )
        statusWrapper.isVisible = visible
    }
}