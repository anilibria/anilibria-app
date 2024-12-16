package com.lapism.search.widget

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.transition.ChangeBounds
import androidx.transition.ChangeTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.lapism.search.ChangeElevation
import com.lapism.search.ChangeOutlineRadius
import com.lapism.search.MarginsType
import com.lapism.search.R
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout

class SearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : SearchLayout(context, attrs, defStyleAttr, defStyleRes), CoordinatorLayout.AttachedBehavior {

    // *********************************************************************************************
    private val viewBehavior = SearchBehavior<SearchView>()

    // *********************************************************************************************
    init {
        inflate(context, R.layout.search_view, this)
        init()

        setDefault()

        val transition = LayoutTransition()
        transition.enableTransitionType(LayoutTransition.CHANGING)
        transition.setDuration(getAnimationDuration())

        //this.layoutTransition = transition
    }

    // *********************************************************************************************
    private fun setDefault() {
        applyMarginsType(MarginsType.Toolbar)
        setCardElevation(getDimension(R.dimen.search_elevation))
        setCardRadius(getDimension(R.dimen.search_shape_rounded))
        setFieldHeight(getDimensionPixelSize(R.dimen.search_layout_height))
    }

    // *********************************************************************************************
    override fun addFocus() {
        mOnFocusChangeListener?.onFocusChange(true)
        TransitionManager.beginDelayedTransition(binding.searchFrame, SuperTransition())

        applyMarginsType(MarginsType.NoneToolbar)
        setCardRadius(getDimension(R.dimen.search_shape_none))
        setCardElevation(getDimension(R.dimen.search_elevation_focus))

        val paddingLeftRight = getDimensionPixelSize(R.dimen.search_key_line_16)
        val paddingTop = getDimensionPixelSize(R.dimen.search_key_line_4)
        binding.input.updateLayoutParams<MarginLayoutParams> {
            marginEnd = paddingLeftRight
            marginStart = paddingLeftRight
        }
        binding.field.updateLayoutParams<MarginLayoutParams> {
            topMargin = paddingTop
        }

        binding.shadow.isVisible = true
        binding.contentDivider.isVisible = true

        showContent()
        showKeyboard()
    }

    override fun removeFocus() {
        mOnFocusChangeListener?.onFocusChange(false)
        TransitionManager.beginDelayedTransition(binding.searchFrame, SuperTransition())

        applyMarginsType(MarginsType.Toolbar)
        setCardRadius(getDimension(R.dimen.search_shape_rounded))
        setCardElevation(context.resources.getDimension(R.dimen.search_elevation))

        binding.input.updateLayoutParams<MarginLayoutParams> {
            marginEnd = 0
            marginStart = 0
        }
        binding.field.updateLayoutParams<MarginLayoutParams> {
            topMargin = 0
        }

        binding.shadow.isVisible = false
        binding.contentDivider.isVisible = false

        hideContent()
        hideKeyboard()
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return viewBehavior
    }

    class SuperTransition : TransitionSet() {
        init {
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeElevation())
            addTransition(ChangeOutlineRadius())
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
}