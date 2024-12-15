package com.lapism.search.internal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    protected val binding by viewBinding<CommonHierarchyBinding>(attachToRoot = false)

    // *********************************************************************************************
    protected var mOnFocusChangeListener: OnFocusChangeListener? = null

    private var mAnimationDuration: Long = 300L
    private var mOnQueryTextListener: OnQueryTextListener? = null
    private var mOnNavigationClickListener: OnNavigationClickListener? = null
    private var mOnClearClickListener: OnClearClickListener? = null
    private var mOnMenuClickListener: OnMenuClickListener? = null

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
        binding.navigationButton.setOnClickListener(this)

        binding.clearButton.isVisible = false
        binding.clearButton.setOnClickListener(this)

        binding.menuButton.isVisible = false
        binding.menuButton.setOnClickListener(this)

        binding.input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                this@SearchLayout.onTextChanged(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
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
        //isClickable = true TODO
        setOnClickListener(this)
    }


    // *********************************************************************************************
    fun setNavigationIcon(icon: NavigationIcon) {
        val icRes = when (icon) {
            NavigationIcon.Arrow -> R.drawable.search_ic_outline_arrow_back_24px
            NavigationIcon.Search -> R.drawable.search_ic_outline_search_24px
        }
        setNavigationIconImageDrawable(ContextCompat.getDrawable(context, icRes))
    }

    fun setNavigationIconImageResource(@DrawableRes resId: Int) {
        binding.navigationButton.setImageResource(resId)
    }

    fun setNavigationIconImageDrawable(drawable: Drawable?) {
        binding.navigationButton.setImageDrawable(drawable)
    }

    fun setNavigationIconColorFilter(color: Int) {
        binding.navigationButton.setColorFilter(color)
    }

    fun setNavigationIconColorFilter(color: Int, mode: PorterDuff.Mode) {
        binding.navigationButton.setColorFilter(color, mode)
    }

    fun setNavigationIconColorFilter(cf: ColorFilter?) {
        binding.navigationButton.colorFilter = cf
    }

    fun clearNavigationIconColorFilter() {
        binding.navigationButton.clearColorFilter()
    }

    fun setNavigationIconContentDescription(contentDescription: CharSequence) {
        binding.navigationButton.contentDescription = contentDescription
    }

    // *********************************************************************************************
    fun setClearIconImageResource(@DrawableRes resId: Int) {
        binding.clearButton.setImageResource(resId)
    }

    fun setClearIconImageDrawable(drawable: Drawable?) {
        binding.clearButton.setImageDrawable(drawable)
    }

    fun setClearIconColorFilter(color: Int) {
        binding.clearButton.setColorFilter(color)
    }

    fun setClearIconColorFilter(color: Int, mode: PorterDuff.Mode) {
        binding.clearButton.setColorFilter(color, mode)
    }

    fun setClearIconColorFilter(cf: ColorFilter?) {
        binding.clearButton.colorFilter = cf
    }

    fun clearClearIconColorFilter() {
        binding.clearButton.clearColorFilter()
    }

    fun setClearIconContentDescription(contentDescription: CharSequence) {
        binding.clearButton.contentDescription = contentDescription
    }

    // *********************************************************************************************
    fun setMenuIconImageResource(@DrawableRes resId: Int) {
        binding.menuButton.setImageResource(resId)
    }

    fun setMenuIconImageDrawable(drawable: Drawable?) {
        binding.menuButton.setImageDrawable(drawable)
    }

    fun setMenuIconColorFilter(color: Int) {
        binding.menuButton.setColorFilter(color)
    }

    fun setMenuIconColorFilter(color: Int, mode: PorterDuff.Mode) {
        binding.menuButton.setColorFilter(color, mode)
    }

    fun setMenuIconColorFilter(cf: ColorFilter?) {
        binding.menuButton.colorFilter = cf
    }

    fun clearMenuIconColorFilter() {
        binding.menuButton.clearColorFilter()
    }

    fun setMenuIconContentDescription(contentDescription: CharSequence) {
        binding.menuButton.contentDescription = contentDescription
    }

    // *********************************************************************************************
    fun setAdapterLayoutManager(layout: RecyclerView.LayoutManager?) {
        binding.content.layoutManager = layout
    }

    fun setAdapterHasFixedSize(hasFixedSize: Boolean) {
        binding.content.setHasFixedSize(hasFixedSize)
    }

    /**
     * DividerItemDecoration class
     */
    fun addAdapterItemDecoration(decor: RecyclerView.ItemDecoration) {
        binding.content.addItemDecoration(decor)
    }

    fun removeAdapterItemDecoration(decor: RecyclerView.ItemDecoration) {
        binding.content.removeItemDecoration(decor)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        binding.content.adapter = adapter
    }

    fun getAdapter(): RecyclerView.Adapter<*>? {
        return binding.content.adapter
    }

    // *********************************************************************************************
    /**
     * Typeface.NORMAL
     * Typeface.BOLD
     * Typeface.ITALIC
     * Typeface.BOLD_ITALIC
     *
     * Typeface.DEFAULT
     * Typeface.DEFAULT_BOLD
     * Typeface.SANS_SERIF
     * Typeface.SERIF
     * Typeface.MONOSPACE
     *
     * Typeface.create(Typeface.NORMAL, Typeface.DEFAULT)
     *
     * TODO PARAMETERS NAME
     */
    fun setTextTypeface(typeface: Typeface?) {
        binding.input.typeface = typeface
    }

    fun getTextTypeface(): Typeface? {
        return binding.input.typeface
    }

    fun setTextInputType(inputType: Int) {
        binding.input.inputType = inputType
    }

    fun getTextInputType(): Int {
        return binding.input.inputType
    }

    fun setTextImeOptions(imeOptions: Int) {
        binding.input.imeOptions = imeOptions
    }

    fun getTextImeOptions(): Int {
        return binding.input.imeOptions
    }

    fun setTextQuery(query: CharSequence?, submit: Boolean) {
        binding.input.setText(query)
        if (query != null) {
            binding.input.setSelection(binding.input.length())
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery()
        }
    }

    fun getTextQuery(): CharSequence? {
        return binding.input.text
    }

    fun setTextHint(hint: CharSequence?) {
        binding.input.hint = hint
    }

    fun getTextHint(): CharSequence? {
        return binding.input.hint
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.input.setTextColor(color)
    }

    fun setTextSize(size: Float) {
        binding.input.textSize = size
    }

    fun setTextGravity(gravity: Int) {
        binding.input.gravity = gravity
    }

    fun setTextHint(@StringRes hint: Int) {
        binding.input.setHint(hint)
    }

    fun setTextHintColor(@ColorInt color: Int) {
        binding.input.setHintTextColor(color)
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

    fun setOnNavigationClickListener(listener: OnNavigationClickListener) {
        mOnNavigationClickListener = listener
    }

    fun setOnClearClickListener(listener: OnClearClickListener) {
        mOnClearClickListener = listener
    }

    fun setOnMenuClickListener(listener: OnMenuClickListener) {
        mOnMenuClickListener = listener
        binding.menuButton.isVisible = true
    }

    // *********************************************************************************************
    fun showKeyboard() {
        if (!isInEditMode) {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(
                binding.input,
                InputMethodManager.SHOW_IMPLICIT
                //InputMethodManager.RESULT_UNCHANGED_SHOWN todo
            )
        }
    }

    fun hideKeyboard() {
        if (!isInEditMode) {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                windowToken,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    // *********************************************************************************************
    protected fun getAnimationDuration(): Long {
        return mAnimationDuration
    }

    protected fun filter(constraint: CharSequence) {
        if (mOnQueryTextListener != null) {
            mOnQueryTextListener?.onQueryTextChange(constraint)
        }
    }

    protected fun setLayoutHeight(newHeight: Int) {
        binding.field.updateLayoutParams {
            height = newHeight
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    protected fun showAdapter() {
        if (binding.content.adapter != null) {
            binding.content.isVisible = true
        }
    }

    protected fun hideAdapter() {
        if (binding.content.adapter != null) {
            binding.content.isVisible = false
        }
    }

    // *********************************************************************************************
    // TODO - SET AS PUBLIC IN THE FUTURE RELEASE
    private fun setAnimationDuration(animationDuration: Long) {
        mAnimationDuration = animationDuration
    }

    private fun onTextChanged(newText: CharSequence) {
        filter(newText)
    }

    private fun onSubmitQuery() {
        val query = binding.input.text
        if (query == null || TextUtils.getTrimmedLength(query) <= 0) return
        if (mOnQueryTextListener?.onQueryTextSubmit(query.toString()) == false) {
            binding.input.text = query
        }
    }

    // *********************************************************************************************
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SearchViewSavedState(superState)
        ss.query = binding.input.text?.toString()
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
        if (state.query != null) {
            setTextQuery(state.query, false)
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

    override fun onClick(view: View?) {
        if (view === binding.navigationButton) {
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
        } else if (view === binding.clearButton) {
            binding.input.clearText()
            mOnClearClickListener?.onClearClick()
        } else if (view === binding.menuButton) {
            mOnMenuClickListener?.onMenuClick()
        } else if (view === binding.shadow) {
            binding.input.clearFocus()
            binding.input.clearText()
        }
    }

    // *********************************************************************************************
    interface OnFocusChangeListener {

        fun onFocusChange(hasFocus: Boolean)
    }

    interface OnQueryTextListener {

        fun onQueryTextChange(newText: CharSequence): Boolean

        fun onQueryTextSubmit(query: CharSequence): Boolean
    }

    interface OnNavigationClickListener {

        fun onNavigationClick()
    }

    interface OnClearClickListener {

        fun onClearClick()
    }

    interface OnMenuClickListener {

        fun onMenuClick()
    }

}
