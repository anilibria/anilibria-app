package ru.radiationx.media.mobile.controllers

import android.util.Log
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener

class LockController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val container: ViewGroup,
    private val button: MaterialButton,
) : PlayerAttachListener {

    private var tapJob: Job? = null

    var onUnlockClick: (() -> Unit)? = null

    init {
        button.setOnClickListener {
            onUnlockClick?.invoke()
        }

        container.setOnClickListener {
            onInteraction()
        }
    }

    fun onInteraction() {
        tapJob?.cancel()
        tapJob = null
        updateState(true)
        tapJob = coroutineScope.launch {
            updateState(true)
            delay(2000)
            updateState(false)
        }
    }

    private fun updateState(state: Boolean) {
        if (button.isVisible == state) {
            return
        }
        TransitionManager.endTransitions(container)
        TransitionManager.beginDelayedTransition(container, AutoTransition().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
        })
        button.isVisible = state
    }

}