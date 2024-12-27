package searchbar.widget

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import searchbar.R
import searchbar.internal.MarginsType
import searchbar.internal.SearchLayout
import searchbar.internal.animations.SuperTransition
import searchbar.internal.behavior.SearchBehavior

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
    }

    // *********************************************************************************************
    private fun setDefault() {
        applyDefaultLayout()
        setFieldHeight(getDimensionPixelSize(R.dimen.search_layout_height))
        binding.shadow.setOnClickListener {
            binding.input.clearFocus()
            binding.input.clearText()
        }
        binding.shadow.isVisible = false
        binding.contentDivider.isVisible = false
        fieldInsetsChanged()
    }

    // *********************************************************************************************
    override fun addFocus() {
        mOnFocusChangeListener?.onFocusChange(true)
        TransitionManager.beginDelayedTransition(binding.searchFrame, createTransition())
        applyFocusedLayout()
        binding.shadow.isVisible = true
        binding.contentDivider.isVisible = true
        showContent()
        showKeyboard()
    }

    override fun removeFocus() {
        mOnFocusChangeListener?.onFocusChange(false)
        TransitionManager.beginDelayedTransition(binding.searchFrame, createTransition())
        applyDefaultLayout()
        binding.shadow.isVisible = false
        binding.contentDivider.isVisible = false
        hideContent()
        hideKeyboard()
    }

    override fun fieldInsetsChanged() {
        if (binding.input.hasFocus()) {
            applyFocusedLayout()
        } else {
            applyDefaultLayout()
        }
    }

    private fun applyDefaultLayout() {
        applyMarginsType(MarginsType.Toolbar) {
            leftMargin += mFieldInsets.left
            rightMargin += mFieldInsets.right
        }
        setCardRadius(getDimension(R.dimen.search_shape_rounded))
        setCardElevation(context.resources.getDimension(R.dimen.search_elevation))

        binding.input.updateLayoutParams<MarginLayoutParams> {
            marginEnd = 0
            marginStart = 0
        }
        binding.field.updateLayoutParams<MarginLayoutParams> {
            leftMargin = 0
            topMargin = 0
            rightMargin = 0
        }
    }

    private fun applyFocusedLayout() {
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
            leftMargin = mFieldInsets.left
            topMargin = paddingTop
            rightMargin = mFieldInsets.right
        }
    }

    private fun createTransition(): Transition = SuperTransition().apply {
        addTransition(Fade().apply {
            addTarget(binding.shadow)
        })
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return viewBehavior
    }
}