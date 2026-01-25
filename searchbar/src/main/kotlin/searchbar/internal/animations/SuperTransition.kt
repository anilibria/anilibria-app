package searchbar.internal.animations

import android.view.animation.AccelerateDecelerateInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet

internal class SuperTransition : TransitionSet() {
    init {
        addTransition(ChangeBounds())
        addTransition(ChangeTransform())
        addTransition(ChangeElevation())
        addTransition(ChangeOutlineRadius())
        interpolator = AccelerateDecelerateInterpolator()
    }
}