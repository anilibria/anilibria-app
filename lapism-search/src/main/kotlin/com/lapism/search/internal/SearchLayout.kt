package com.lapism.search.internal

import android.content.Context
import android.graphics.Rect
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
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

    protected var mFieldInsets = Insets.NONE

    // *********************************************************************************************
    protected var mOnFocusChangeListener: OnFocusChangeListener? = null

    private var mOnQueryTextListener: OnQueryTextListener? = null
    private var mOnQuerySubmitListener: OnQuerySubmitListener? = null
    private var mOnNavigationClickListener: OnNavigationClickListener? = null
    private var mOnClearClickListener: OnClearClickListener? = null

    // *********************************************************************************************
    protected abstract fun addFocus()

    protected abstract fun removeFocus()

    protected abstract fun fieldInsetsChanged()

    protected fun init() {
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
        binding.clearButton.setImageResource(R.drawable.search_ic_outline_clear_24px)
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
        binding.shadow.setBackgroundColor(ContextCompat.getColor(context, R.color.search_shadow))

        binding.filler.isVisible = false

        binding.progress.isVisible = false

        isFocusable = true
        isFocusableInTouchMode = true
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
            setQuery(it, false)
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
    fun setNavigationIcon(icon: NavigationIcon) {
        val icRes = when (icon) {
            NavigationIcon.Arrow -> R.drawable.search_ic_outline_arrow_back_24px
            NavigationIcon.Search -> R.drawable.search_ic_outline_search_24px
        }
        binding.navigationButton.setImageDrawable(ContextCompat.getDrawable(context, icRes))
    }

    // *********************************************************************************************
    fun setContentAdapter(adapter: RecyclerView.Adapter<*>?) {
        binding.content.adapter = adapter
    }

    fun setLoading(visible: Boolean) {
        binding.progress.isVisible = visible
    }

    fun setFieldInsets(insets: Insets) {
        mFieldInsets = insets
        fieldInsetsChanged()
    }

    // *********************************************************************************************

    fun setQuery(query: String, submit: Boolean) {
        binding.input.setText(query)
        binding.input.setSelection(binding.input.length())
        if (submit) {
            onSubmitQuery()
        }
    }

    fun getQuery(): String {
        return binding.input.getQuery()
    }

    fun setHint(hint: String) {
        binding.input.hint = hint
    }

    fun setHint(@StringRes hint: Int) {
        binding.input.setHint(hint)
    }

    // *********************************************************************************************
    fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        mOnFocusChangeListener = listener
    }

    fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        mOnQueryTextListener = listener
    }

    fun setOnQuerySubmitListener(listener: OnQuerySubmitListener?) {
        mOnQuerySubmitListener = listener
    }

    fun setOnNavigationClickListener(listener: OnNavigationClickListener?) {
        mOnNavigationClickListener = listener
    }

    fun setOnClearClickListener(listener: OnClearClickListener?) {
        mOnClearClickListener = listener
    }

    // *********************************************************************************************

    protected fun setCardElevation(elevation: Float) {
        binding.cardView.cardElevation = elevation
    }

    protected fun setCardRadius(radius: Float) {
        binding.cardView.radius = radius
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
    protected fun setFieldHeight(newHeight: Int) {
        binding.field.updateLayoutParams {
            height = newHeight
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    protected fun showContent() {
        if (binding.content.adapter == null) return
        binding.content.isVisible = true
    }

    protected fun hideContent() {
        if (binding.content.adapter == null) return
        binding.content.isVisible = false
    }

    protected fun applyMarginsType(
        type: MarginsType,
        block: (MarginLayoutParams.() -> Unit)? = null
    ) {
        val toolbarNone = getDimensionPixelSize(R.dimen.search_margins_toolbar_none)
        val menuItemNone = getDimensionPixelSize(R.dimen.search_margins_menu_item_none)
        val toolbarLeftRight = getDimensionPixelSize(R.dimen.search_margins_toolbar_left_right)
        val toolbarTopBottom = getDimensionPixelSize(R.dimen.search_margins_toolbar_top_bottom)
        val menuItem = getDimensionPixelSize(R.dimen.search_margins_menu_item)
        binding.cardView.updateLayoutParams<MarginLayoutParams> {
            when (type) {
                MarginsType.NoneToolbar -> {
                    leftMargin = toolbarNone
                    topMargin = toolbarNone
                    rightMargin = toolbarNone
                    bottomMargin = toolbarNone
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                MarginsType.NoneMenuItem -> {
                    leftMargin = menuItemNone
                    topMargin = menuItemNone
                    rightMargin = menuItemNone
                    bottomMargin = menuItemNone
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                MarginsType.Toolbar -> {
                    leftMargin = toolbarLeftRight
                    topMargin = toolbarTopBottom
                    rightMargin = toolbarLeftRight
                    bottomMargin = toolbarTopBottom
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                MarginsType.MenuItem -> {
                    leftMargin = menuItem
                    topMargin = menuItem
                    rightMargin = menuItem
                    bottomMargin = menuItem
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
            block?.invoke(this)
        }
    }

    protected fun getDimensionPixelSize(@DimenRes dimenRes: Int): Int {
        return context.resources.getDimensionPixelSize(dimenRes)
    }

    protected fun getDimension(@DimenRes dimenRes: Int): Float {
        return context.resources.getDimension(dimenRes)
    }

    // *********************************************************************************************
    private fun onTextChanged(newText: String) {
        binding.clearButton.isVisible = newText.isNotEmpty()
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

}
