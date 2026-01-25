package searchbar.internal.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.cardview.widget.CardView
import androidx.transition.Transition
import androidx.transition.TransitionValues

/**
 * Transitions a view from [startRadius] to [endRadius] through a [ViewOutlineProvider].
 * Note that if the view already has a radius (rounded corners), [startRadius] should match this value
 *
 * @author Stefan de Bruijn
 */
internal class ChangeElevation : Transition {

    private companion object {
        /**
         * Unique key for our start and end values to be kept in [TransitionValues]
         */
        private const val ELEVATION = "ChangeElevation:elevation"

        /**
         * The properties from [TransitionValues] we care about.
         * If the [TransitionValues] from [captureStartValues] and [captureEndValues] do not differ on this property,
         * this [Transition] will not run
         */
        private val PROPERTIES = arrayOf(ELEVATION)

        /**
         * Animator property which will set a rounded outline through a [ViewOutlineProvider]
         */
        private object ElevationProperty : Property<View, Float>(Float::class.java, "elevation") {

            override fun get(view: View): Float = when (view) {
                is CardView -> view.cardElevation
                else -> 0f
            }

            override fun set(view: View, value: Float) {
                val elevation = value.coerceAtLeast(0f)
                when (view) {
                    is CardView -> view.cardElevation = elevation
                }
            }
        }
    }

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun getTransitionProperties(): Array<String> {
        return PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        // Here we get the elevation our transition starts with.
        // Note that this can be the starting value for both the original transition *and the reverse*
        val view = transitionValues.view
        transitionValues.values[ELEVATION] = ElevationProperty.get(view)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        // Here we get the elevation our transition ends with.
        // Note that this can be the ending value for both the original transition *and the reverse*
        val view = transitionValues.view
        transitionValues.values[ELEVATION] = ElevationProperty.get(view)
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator? {
        startValues ?: return null
        endValues ?: return null
        // The view on the start and end values is the same, so it doesn't matter which we use
        val view = endValues.view
        // We want the view to clip to the round rect we're going to set

        // The startRadius/endRadius we are going to animate from/to.
        // Using the start/end value instead of the constructor params allows us to support reversing this animation too
        val sR = startValues.values[ELEVATION] as Float
        val eR = endValues.values[ELEVATION] as Float

        // Animator with Property allows us to adjust properties that aren't directly (available) on the View itself
        return ObjectAnimator.ofFloat(view, ElevationProperty, sR, eR)
    }
}