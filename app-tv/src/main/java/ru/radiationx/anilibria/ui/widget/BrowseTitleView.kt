/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.leanback.widget.SearchOrbView
import androidx.leanback.widget.TitleViewAdapter
import kotlinx.android.synthetic.main.view_titleview.view.*
import ru.radiationx.anilibria.R

open class BrowseTitleView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.browseTitleViewStyle
) : ConstraintLayout(context, attrs, defStyleAttr), TitleViewAdapter.Provider {

    private var flags = TitleViewAdapter.FULL_VIEW_VISIBLE
    private var mHasSearchListener = false
    private val mTitleViewAdapter by lazy { Adapter(this) }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_titleview, this)
        clipToPadding = false
        clipChildren = false
    }

    var title: CharSequence?
        get() = title_text.text
        set(titleText) {
            title_text.text = titleText
            updateBadgeVisibility()
        }

    var badgeDrawable: Drawable?
        get() = title_badge.drawable
        set(drawable) {
            title_badge.setImageDrawable(drawable)
            updateBadgeVisibility()
        }

    val searchAffordanceView: View
        get() = title_orb

    var searchAffordanceColors: SearchOrbView.Colors
        get() = title_orb.orbColors
        set(colors) {
            title_orb.orbColors = colors
        }

    var alert: CharSequence?
        get() = title_alert.text
        set(value) {
            title_alert.text = value
            updateAlertVisibility()
        }

    var other: CharSequence?
        get() = title_other.text
        set(value) {
            title_other.text = value
            updateOtherVisibility()
        }

    fun setOnSearchClickedListener(listener: OnClickListener?) {
        mHasSearchListener = listener != null
        title_orb.setOnOrbClickedListener(listener)
        updateSearchOrbViewVisibility()
    }

    fun setOnAlertClickedListener(listener: OnClickListener?) {
        title_alert.setOnClickListener(listener)
    }

    fun setOnOtherClickedListener(listener: OnClickListener?) {
        title_other.setOnClickListener(listener)
    }

    fun enableAnimation(enable: Boolean) {
        title_orb.enableOrbColorAnimation(enable && title_orb.hasFocus())
    }

    fun updateComponentsVisibility(flags: Int) {
        this.flags = flags
        if (flags and TitleViewAdapter.BRANDING_VIEW_VISIBLE == TitleViewAdapter.BRANDING_VIEW_VISIBLE) {
            updateBadgeVisibility()
        } else {
            title_badge.visibility = View.GONE
            title_text.visibility = View.GONE
        }
        updateSearchOrbViewVisibility()
        updateAlertVisibility()
        updateOtherVisibility()
    }

    private fun updateSearchOrbViewVisibility() {
        val visibility =
            if (mHasSearchListener && flags and TitleViewAdapter.SEARCH_VIEW_VISIBLE == TitleViewAdapter.SEARCH_VIEW_VISIBLE) View.VISIBLE else View.INVISIBLE
        title_orb.visibility = visibility
    }

    private fun updateBadgeVisibility() {
        val drawable = title_badge.drawable
        if (drawable != null) {
            title_badge.visibility = View.VISIBLE
            title_text.visibility = View.GONE
        } else {
            title_badge.visibility = View.GONE
            title_text.visibility = View.VISIBLE
        }
    }

    private fun updateAlertVisibility() {
        title_alert.isVisible = !alert.isNullOrEmpty()
    }

    private fun updateOtherVisibility() {
        title_other.isVisible = !other.isNullOrEmpty()
    }

    override fun getTitleViewAdapter(): TitleViewAdapter = mTitleViewAdapter

    override fun focusSearch(focused: View, direction: Int): View? {
        Log.e("kokoko","focusSearch $direction, $focused")
        if ((direction == View.FOCUS_LEFT || direction == View.FOCUS_RIGHT || direction == View.FOCUS_UP || direction == View.FOCUS_DOWN)) {
            var nextView: View? = when (direction) {
                View.FOCUS_LEFT -> findViewById(focused.nextFocusLeftId)
                View.FOCUS_RIGHT -> findViewById(focused.nextFocusRightId)
                View.FOCUS_DOWN -> findViewById(focused.nextFocusDownId)
                View.FOCUS_UP -> findViewById(focused.nextFocusUpId)
                else -> null
            }
            Log.e("kokoko","focusSearch next $direction, $nextView")
            if (focused.parent == this && (nextView == null || !nextView.isVisible || !(nextView.isFocusable || nextView is ViewGroup))) {
                nextView = when (direction) {
                    View.FOCUS_LEFT -> findViewById(focused.nextFocusUpId)
                    View.FOCUS_RIGHT -> findViewById(focused.nextFocusDownId)
                    // Имхо так лучше
                    //View.FOCUS_DOWN -> findViewById(focused.nextFocusRightId)
                    //View.FOCUS_UP -> findViewById(focused.nextFocusLeftId)
                    else -> null
                }
            }

            Log.e("kokoko","focusSearch final next $direction, $nextView")

            if (nextView != null && (nextView.isFocusable || nextView is ViewGroup)) {
                return nextView
            }
        }
        return super.focusSearch(focused, direction)
    }

    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return searchAffordanceView.requestFocus() || super.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    class Adapter(private val titleView: BrowseTitleView) : TitleViewAdapter() {

        fun setAlert(alertText: CharSequence?) {
            titleView.alert = alertText
        }

        fun getAlert(): CharSequence? = titleView.alert

        fun setOnAlertClickedListener(listener: OnClickListener?) = titleView.setOnAlertClickedListener(listener)

        fun setOther(otherText: CharSequence?) {
            titleView.other = otherText
        }

        fun getOther(): CharSequence? = titleView.other

        fun setOnOtherClickedListener(listener: OnClickListener?) = titleView.setOnOtherClickedListener(listener)

        override fun getSearchAffordanceView(): View = titleView.searchAffordanceView
        override fun setOnSearchClickedListener(listener: OnClickListener?) = titleView.setOnSearchClickedListener(listener)
        override fun setAnimationEnabled(enable: Boolean) = titleView.enableAnimation(enable)
        override fun getBadgeDrawable(): Drawable? = titleView.badgeDrawable
        override fun getSearchAffordanceColors(): SearchOrbView.Colors = titleView.searchAffordanceColors
        override fun getTitle(): CharSequence? = titleView.title
        override fun setBadgeDrawable(drawable: Drawable?) {
            titleView.badgeDrawable = drawable
        }

        override fun setSearchAffordanceColors(colors: SearchOrbView.Colors) {
            titleView.searchAffordanceColors = colors
        }

        override fun setTitle(titleText: CharSequence?) {
            titleView.title = titleText
        }

        override fun updateComponentsVisibility(flags: Int) = titleView.updateComponentsVisibility(flags)
    }
}