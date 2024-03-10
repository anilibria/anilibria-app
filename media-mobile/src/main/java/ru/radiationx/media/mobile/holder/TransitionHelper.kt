package ru.radiationx.media.mobile.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet

internal class TransitionHelper(
    private val transitionRoot: ViewGroup,
    private val targetViews: List<View>,
) {

    private val overlayTransition = AutoTransition().apply {
        ordering = TransitionSet.ORDERING_TOGETHER
        duration = 200
        targetViews.forEach {
            addTarget(it)
        }
    }

    init {
        initInvisibleViewsTransitionFix()
    }

    fun beginDelayedTransition() {
        TransitionManager.beginDelayedTransition(transitionRoot, overlayTransition)
    }

    fun endTransition() {
        TransitionManager.endTransitions(transitionRoot)
    }

    // Problem: after screen rotating views has a wrong bounds, therefore transition looks weird
    // Fix for invisible views apearing after again visible
    // Needs reset isLaidOut and view bounds for disable "weird" ChangeBounds transition
    private fun initInvisibleViewsTransitionFix() {
        // detect layout changes (screen rotation, window bounds change, etc.)
        transitionRoot.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom) {
                return@addOnLayoutChangeListener
            }
            val parent = (transitionRoot.parent as ViewGroup)
            val index = parent.indexOfChild(transitionRoot)

            // detach from parent for isLaidOut == null
            parent.removeViewAt(index)

            // reset invisible views for width&height == 0
            targetViews.forEach {
                if (it.isVisible) {
                    return@forEach
                }
                it.top = 0
                it.left = 0
                it.right = 0
                it.bottom = 0
            }

            parent.addView(transitionRoot, index)
        }
    }
}