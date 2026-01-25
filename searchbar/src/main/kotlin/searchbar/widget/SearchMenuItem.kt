package searchbar.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.transition.MaterialSharedAxis
import searchbar.NavigationIcon
import searchbar.R
import searchbar.internal.MarginsType
import searchbar.internal.SearchLayout

class SearchMenuItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SearchLayout(context, attrs, defStyleAttr, defStyleRes) {

    // *********************************************************************************************
    init {
        inflate(context, R.layout.search_menu_item, this)
        init()
        setDefault()
    }

    // *********************************************************************************************
    fun show() {
        binding.input.requestFocus()
    }

    // *********************************************************************************************
    private fun setDefault() {
        setNavigationIcon(NavigationIcon.Arrow)
        setCardElevation(getDimension(R.dimen.search_elevation))
        setCardRadius(getDimension(R.dimen.search_shape_none))
        setFieldHeight(getDimensionPixelSize(R.dimen.search_layout_height_focus))
        applyDefaultLayout()
        binding.shadow.setOnClickListener {
            binding.input.clearFocus()
            binding.input.clearText()
        }
        binding.filler.isVisible = false
        binding.shadow.isVisible = false
        binding.contentDivider.isVisible = false
        binding.cardView.isVisible = false
        fieldInsetsChanged()
    }

    // *********************************************************************************************
    override fun addFocus() {
        TransitionManager.endTransitions(binding.searchFrame)
        binding.filler.isVisible = true
        binding.searchFrame.doOnLayout {
            mOnFocusChangeListener?.onFocusChange(true)
            TransitionManager.beginDelayedTransition(binding.searchFrame, createTransition(true))
            binding.cardView.isVisible = true
            binding.contentDivider.isVisible = true
            binding.shadow.isVisible = true
            showContent()
            showKeyboard()
        }
    }

    override fun removeFocus() {
        TransitionManager.endTransitions(binding.searchFrame)
        TransitionManager.beginDelayedTransition(
            binding.searchFrame,
            createTransition(false).addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)
                    mOnFocusChangeListener?.onFocusChange(false)
                    binding.filler.isVisible = false
                    hideKeyboard()
                    hideContent()
                }
            })
        )
        binding.cardView.isVisible = false
        binding.contentDivider.isVisible = false
        binding.shadow.isVisible = false
    }

    override fun fieldInsetsChanged() {
        applyDefaultLayout()
    }

    private fun applyDefaultLayout() {
        applyMarginsType(MarginsType.NoneMenuItem)
        val paddingLeftRight = getDimensionPixelSize(R.dimen.search_key_line_16)
        binding.input.setPadding(paddingLeftRight, 0, paddingLeftRight, 0)
        binding.field.updateLayoutParams<MarginLayoutParams> {
            leftMargin = mFieldInsets.left
            rightMargin = mFieldInsets.right
        }
    }

    private fun createTransition(focus: Boolean): Transition = TransitionSet().apply {
        ordering = TransitionSet.ORDERING_TOGETHER
        addTransition(MaterialSharedAxis(MaterialSharedAxis.Y, !focus).apply {
            addTarget(binding.cardView)
        })
        addTransition(Fade().apply {
            addTarget(binding.shadow)
        })
    }
}
