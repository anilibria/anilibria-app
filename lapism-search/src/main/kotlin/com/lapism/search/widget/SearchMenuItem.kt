package com.lapism.search.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.lapism.search.MarginsType
import com.lapism.search.R
import com.lapism.search.SearchUtils
import com.lapism.search.internal.SearchAnimation
import com.lapism.search.internal.SearchLayout

// TODO CircularRevealLinearLayout
class SearchMenuItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SearchLayout(context, attrs, defStyleAttr, defStyleRes) {

    // *********************************************************************************************
    private var mShadowVisibility: Boolean = true
    private var mMenuItemCx = -1

    // *********************************************************************************************
    init {
        inflate(context, R.layout.search_menu_item, this)
        init()

        setClearIconImageResource(R.drawable.search_ic_outline_clear_24px)
        binding.shadow.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.search_shadow
            )
        )

        setDefault()
        binding.contentDivider.isVisible = true
    }

    // *********************************************************************************************
    fun requestFocus(menuItem: MenuItem) {
        if (!isFocusable) {
            return
        } else {
            getMenuItemPosition(menuItem.itemId)
            binding.input.requestFocus()
        }
    }

    fun setShadowVisibility(visibility: Boolean) {
        binding.shadow.isVisible = visibility
        mShadowVisibility = visibility
    }

    // *********************************************************************************************
    private fun setDefault() {
        applyMarginsType(MarginsType.NoneMenuItem)
        elevation =
            context.resources.getDimensionPixelSize(R.dimen.search_elevation_focus).toFloat()
        setBackgroundRadius(resources.getDimensionPixelSize(R.dimen.search_shape_none).toFloat())
        setLayoutHeight(context.resources.getDimensionPixelSize(R.dimen.search_layout_height_focus))
        val paddingLeftRight = context.resources.getDimensionPixelSize(R.dimen.search_key_line_16)
        binding.input.setPadding(paddingLeftRight, 0, paddingLeftRight, 0)

        binding.shadow.setOnClickListener(this)
        binding.cardView.isVisible = false
        this.isVisible = false
    }

    private fun getMenuItemPosition(menuItemId: Int) {
        var viewParent = parent
        while (viewParent is View) {
            val parent = viewParent as View
            val view = parent.findViewById<View>(menuItemId)
            if (view != null) {
                mMenuItemCx = getCenterX(view)
                break
            }
            viewParent = viewParent.getParent()
        }
    }

    private fun getCenterX(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location[0] + view.width / 2
    }

    // *********************************************************************************************
    override fun addFocus() {
        this.isVisible = true
        binding.cardView.isVisible = true

        val animation = SearchAnimation()
        animation.setOnAnimationListener(object :
            SearchAnimation.OnAnimationListener {
            override fun onAnimationStart() {
                mOnFocusChangeListener?.onFocusChange(true)
                if (mShadowVisibility) {
                    SearchUtils.fadeAddFocus(binding.shadow, getAnimationDuration())
                }
            }

            override fun onAnimationEnd() {
                showAdapter()
                showKeyboard()
            }
        })

        val viewTreeObserver = binding.cardView.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    animation.start(
                        context,
                        binding.field,
                        binding.cardView,
                        mMenuItemCx,
                        getAnimationDuration(),
                        true
                    )

                    binding.cardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    override fun removeFocus() {
        val animation = SearchAnimation()
        animation.setOnAnimationListener(object : SearchAnimation.OnAnimationListener {
            override fun onAnimationStart() {
                if (mShadowVisibility) {
                    SearchUtils.fadeRemoveFocus(binding.shadow, getAnimationDuration())
                }
                hideKeyboard()
                hideAdapter()
                mOnFocusChangeListener?.onFocusChange(false)
            }

            override fun onAnimationEnd() {
                binding.cardView.isVisible = false
                this@SearchMenuItem.isVisible = false
            }
        })
        animation.start(
            context,
            binding.field,
            binding.cardView,
            mMenuItemCx,
            getAnimationDuration(),
            false
        )
    }

}
