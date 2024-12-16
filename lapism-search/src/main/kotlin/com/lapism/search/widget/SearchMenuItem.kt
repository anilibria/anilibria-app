package com.lapism.search.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.lapism.search.internal.MarginsType
import com.lapism.search.NavigationIcon
import com.lapism.search.R
import com.lapism.search.internal.SearchUtils
import com.lapism.search.internal.animations.SearchAnimation
import com.lapism.search.internal.SearchLayout

class SearchMenuItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SearchLayout(context, attrs, defStyleAttr, defStyleRes) {

    // *********************************************************************************************
    private var mMenuItemCx = -1

    // *********************************************************************************************
    init {
        inflate(context, R.layout.search_menu_item, this)
        init()
        setDefault()
    }

    // *********************************************************************************************
    fun requestFocus(menuItem: MenuItem) {
        if (!isFocusable) return
        getMenuItemPosition(menuItem.itemId)
        binding.input.requestFocus()
    }

    // *********************************************************************************************
    private fun setDefault() {
        setNavigationIcon(NavigationIcon.Arrow)
        setCardElevation(getDimension(R.dimen.search_elevation_focus))
        setCardRadius(getDimension(R.dimen.search_shape_none))
        setFieldHeight(getDimensionPixelSize(R.dimen.search_layout_height_focus))
        applyDefaultLayout()
        binding.shadow.setOnClickListener {
            binding.input.clearFocus()
            binding.input.clearText()
        }
        binding.contentDivider.isVisible = true
        binding.cardView.isVisible = false
        this.isVisible = false
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
                SearchUtils.fadeAddFocus(binding.shadow, getAnimationDuration())
            }

            override fun onAnimationEnd() {
                showContent()
                showKeyboard()
            }
        })

        val viewTreeObserver = binding.cardView.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    animation.start(binding, mMenuItemCx, getAnimationDuration(), true)
                    binding.cardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    override fun removeFocus() {
        val animation = SearchAnimation()
        animation.setOnAnimationListener(object : SearchAnimation.OnAnimationListener {
            override fun onAnimationStart() {
                SearchUtils.fadeRemoveFocus(binding.shadow, getAnimationDuration())
                hideKeyboard()
                hideContent()
                mOnFocusChangeListener?.onFocusChange(false)
            }

            override fun onAnimationEnd() {
                binding.cardView.isVisible = false
                this@SearchMenuItem.isVisible = false
            }
        })
        animation.start(binding, mMenuItemCx, getAnimationDuration(), false)
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

}
