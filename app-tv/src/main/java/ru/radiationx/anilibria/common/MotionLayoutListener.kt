package ru.radiationx.anilibria.common

import androidx.constraintlayout.motion.widget.MotionLayout

open class MotionLayoutListener : MotionLayout.TransitionListener {
    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {}
    override fun onTransitionTrigger(
        motionLayout: MotionLayout,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {
    }

    override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {}
    override fun onTransitionChange(
        motionLayout: MotionLayout,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
    }
}