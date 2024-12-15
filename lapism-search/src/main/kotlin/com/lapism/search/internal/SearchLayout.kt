package com.lapism.search.internal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.SoftwareKeyboardControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.lapism.search.MarginsType
import com.lapism.search.NavigationIcon
import com.lapism.search.R
import com.lapism.search.databinding.CommonHierarchyBinding

/**
 * @hide
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class SearchLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    protected val binding by viewBinding<CommonHierarchyBinding>(createMethod = CreateMethod.BIND)

    private val keyboardController by lazy {
        SoftwareKeyboardControllerCompat(binding.input)
    }

    // *********************************************************************************************
    protected var mOnFocusChangeListener: OnFocusChangeListener? = null

    private var mAnimationDuration: Long = 300L
    private var mOnQueryTextListener: OnQueryTextListener? = null
    private var mOnQuerySubmitListener: OnQuerySubmitListener? = null
    private var mOnNavigationClickListener: OnNavigationClickListener? = null
    private var mOnClearClickListener: OnClearClickListener? = null

    private fun getDimensionPixelSize(@DimenRes dimenRes: Int): Int {
        return context.resources.getDimensionPixelSize(dimenRes)
    }

    protected fun applyMarginsType(type: MarginsType) {
        binding.cardView.updateLayoutParams<MarginLayoutParams> {
            when (type) {
                MarginsType.NoneToolbar -> {
                    leftMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_none)
                    topMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_none)
                    rightMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_none)
                    bottomMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_none)
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    // todo mb wrap?
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                MarginsType.NoneMenuItem -> {
                    leftMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item_none)
                    topMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item_none)
                    rightMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item_none)
                    bottomMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item_none)
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                MarginsType.Toolbar -> {
                    leftMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_left_right)
                    topMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_top_bottom)
                    rightMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_left_right)
                    bottomMargin = getDimensionPixelSize(R.dimen.search_margins_toolbar_top_bottom)
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                MarginsType.MenuItem -> {
                    leftMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item)
                    topMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item)
                    rightMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item)
                    bottomMargin = getDimensionPixelSize(R.dimen.search_margins_menu_item)
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        }
    }

    // *********************************************************************************************
    protected abstract fun addFocus()

    protected abstract fun removeFocus()

    protected fun init() {
        binding.clearButton.setImageResource(R.drawable.search_ic_outline_clear_24px)
        binding.navigationButton.setOnClickListener {
            if (binding.input.hasFocus()) {
                val imeVisible = ViewCompat.getRootWindowInsets(this)
                    ?.isVisible(WindowInsetsCompat.Type.ime())
                    ?: false
                if (imeVisible && !binding.input.isTextEmpty()) {
                    hideKeyboard()
                } else {
                    binding.input.clearFocus()
                    binding.input.clearText()
                }
            } else {
                mOnNavigationClickListener?.onNavigationClick()
            }
        }

        binding.clearButton.isVisible = false
        binding.clearButton.setOnClickListener {
            binding.input.clearText()
            mOnClearClickListener?.onClearClick()
        }

        binding.input.doOnTextChanged { text, _, _, _ ->
            this@SearchLayout.onTextChanged(text?.toString().orEmpty())
        }
        binding.input.setOnEditorActionListener { _, _, _ ->
            onSubmitQuery()
            return@setOnEditorActionListener true // true
        }
        binding.input.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addFocus()
            } else {
                removeFocus()
            }
        }

        binding.content.layoutManager = LinearLayoutManager(context)
        binding.content.isVisible = false
        binding.content.isNestedScrollingEnabled = false
        binding.content.itemAnimator = null
        binding.content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })

        binding.contentDivider.isVisible = false

        binding.shadow.isVisible = false

        isFocusable = true
        isFocusableInTouchMode = true
    }


    // *********************************************************************************************
    fun setNavigationIcon(icon: NavigationIcon) {
        val icRes = when (icon) {
            NavigationIcon.Arrow -> R.drawable.search_ic_outline_arrow_back_24px
            NavigationIcon.Search -> R.drawable.search_ic_outline_search_24px
        }
        binding.navigationButton.setImageDrawable(ContextCompat.getDrawable(context, icRes))
    }

    // *********************************************************************************************
    fun setClearIconImageResource(@DrawableRes resId: Int) {

    }

    // *********************************************************************************************
    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        binding.content.adapter = adapter
    }

    fun getAdapter(): RecyclerView.Adapter<*>? {
        return binding.content.adapter
    }

    // *********************************************************************************************

    fun setTextQuery(query: String, submit: Boolean) {
        binding.input.setText(query)
        binding.input.setSelection(binding.input.length())
        if (submit) {
            onSubmitQuery()
        }
    }

    fun getTextQuery(): String {
        return binding.input.getQuery()
    }

    fun setTextHint(hint: String) {
        binding.input.hint = hint
    }

    fun setTextHint(@StringRes hint: Int) {
        binding.input.setHint(hint)
    }

    // *********************************************************************************************
    override fun setBackgroundColor(@ColorInt color: Int) {
        binding.cardView.setCardBackgroundColor(color)
    }

    fun setBackgroundColor(color: ColorStateList?) {
        binding.cardView.setCardBackgroundColor(color)
    }

    // TODO PUBLIC
    override fun setElevation(elevation: Float) {
        binding.cardView.cardElevation = elevation
    }

    // TODO PUBLIC
    protected fun setMaxElevation(maxElevation: Float) {
        binding.cardView.maxCardElevation = maxElevation
    }

    // TODO PUBLIC
    protected fun setBackgroundRadius(radius: Float) {
        binding.cardView.radius = radius
    }

    fun setBackgroundRippleColor(@ColorRes rippleColorResourceId: Int) {
        binding.cardView.setRippleColorResource(rippleColorResourceId)
    }

    fun setBackgroundRippleColorResource(rippleColor: ColorStateList?) {
        binding.cardView.rippleColor = rippleColor
    }

    fun setBackgroundStrokeColor(@ColorInt strokeColor: Int) {
        binding.cardView.strokeColor = strokeColor
    }

    fun setBackgroundStrokeColor(strokeColor: ColorStateList) {
        binding.cardView.setStrokeColor(strokeColor)
    }

    fun setBackgroundStrokeWidth(@Dimension strokeWidth: Int) {
        binding.cardView.strokeWidth = strokeWidth
    }

    // *********************************************************************************************
    fun setBackgroundColorViewOnly(@ColorInt color: Int) {
        binding.field.setBackgroundColor(color)
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.contentDivider.setBackgroundColor(color)
    }

    fun setShadowColor(@ColorInt color: Int) {
        binding.shadow.setBackgroundColor(color)
    }

    // *********************************************************************************************
    fun setOnFocusChangeListener(listener: OnFocusChangeListener) {
        mOnFocusChangeListener = listener
    }

    fun setOnQueryTextListener(listener: OnQueryTextListener) {
        mOnQueryTextListener = listener
    }

    fun setOnQuerySubmitListener(listener: OnQuerySubmitListener) {
        mOnQuerySubmitListener = listener
    }

    fun setOnNavigationClickListener(listener: OnNavigationClickListener) {
        mOnNavigationClickListener = listener
    }

    fun setOnClearClickListener(listener: OnClearClickListener) {
        mOnClearClickListener = listener
    }

    // *********************************************************************************************
    protected fun showKeyboard() {
        if (isInEditMode) return
        keyboardController.show()
    }

    protected fun hideKeyboard() {
        if (isInEditMode) return
        keyboardController.hide()
    }

    // *********************************************************************************************
    protected fun getAnimationDuration(): Long {
        return mAnimationDuration
    }

    protected fun setLayoutHeight(newHeight: Int) {
        binding.field.updateLayoutParams {
            height = newHeight
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    protected fun showAdapter() {
        if (binding.content.adapter == null) return
        binding.content.isVisible = true
    }

    protected fun hideAdapter() {
        if (binding.content.adapter == null) return
        binding.content.isVisible = false
    }

    // *********************************************************************************************
    private fun onTextChanged(newText: String) {
        mOnQueryTextListener?.onQueryTextChange(newText)
    }

    private fun onSubmitQuery() {
        val query = binding.input.getQuery()
        if (TextUtils.getTrimmedLength(query) <= 0) return
        if (mOnQuerySubmitListener?.onQueryTextSubmit(query) == false) {
            binding.input.setText(query)
        }
    }

    // *********************************************************************************************
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SearchViewSavedState(superState)
        ss.query = binding.input.getQuery()
        ss.hasFocus = binding.input.hasFocus()
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SearchViewSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        if (state.hasFocus) {
            binding.input.requestFocus()
        }
        state.query?.also {
            setTextQuery(it, false)
        }
        requestLayout()
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return if (!isFocusable) {
            false
        } else {
            binding.input.requestFocus(direction, previouslyFocusedRect)
        }
    }

    override fun clearFocus() {
        super.clearFocus()
        binding.input.clearFocus()
    }

    // *********************************************************************************************
    fun interface OnFocusChangeListener {

        fun onFocusChange(hasFocus: Boolean)
    }

    fun interface OnQueryTextListener {

        fun onQueryTextChange(newText: String)
    }

    fun interface OnQuerySubmitListener {

        fun onQueryTextSubmit(query: String): Boolean
    }

    fun interface OnNavigationClickListener {

        fun onNavigationClick()
    }

    interface OnClearClickListener {

        fun onClearClick()
    }

    interface OnMenuClickListener {

        fun onMenuClick()
    }

}
