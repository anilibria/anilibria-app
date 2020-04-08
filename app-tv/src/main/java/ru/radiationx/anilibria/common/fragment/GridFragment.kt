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
package ru.radiationx.anilibria.common.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.transition.TransitionHelper
import androidx.leanback.widget.*
import kotlinx.android.synthetic.main.fragment_grid.*
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.screen.ScopedFragment
import kotlin.math.max

/**
 * A fragment for rendering items in a vertical grids.
 */
open class GridFragment : ScopedFragment(R.layout.fragment_grid), BrowseSupportFragment.MainFragmentAdapterProvider {

    private var mAdapter: ObjectAdapter? = null

    private var mGridPresenter: VerticalGridPresenter? = null
    private var mGridViewHolder: VerticalGridPresenter.ViewHolder? = null

    private var mSelectedPosition = -1
    private var mOnItemViewSelectedListener: OnItemViewSelectedListener? = null
    private var mOnItemViewClickedListener: OnItemViewClickedListener? = null
    private val mViewSelectedListener = OnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
        val gridViewHolder = mGridViewHolder ?: return@OnItemViewSelectedListener
        val position = gridViewHolder.gridView.selectedPosition
        gridOnItemSelected(position)
        mOnItemViewSelectedListener?.onItemSelected(itemViewHolder, item, rowViewHolder, row)
    }

    private var mSceneAfterEntranceTransition: Any? = null

    private val mMainFragmentAdapter = object : BrowseSupportFragment.MainFragmentAdapter<GridFragment>(this) {
        override fun setEntranceTransitionState(state: Boolean) {
            this@GridFragment.setEntranceTransitionState(state)
        }
    }

    private val mChildLaidOutListener = OnChildLaidOutListener { parent, view, position, id ->
        if (position == 0) {
            showOrHideTitle()
        }
    }

    var gridPresenter: VerticalGridPresenter?
        get() = mGridPresenter
        set(gridPresenter) {
            requireNotNull(gridPresenter) { "Grid presenter may not be null" }
            mGridPresenter = gridPresenter
            gridPresenter.onItemViewSelectedListener = mViewSelectedListener
            gridPresenter.onItemViewClickedListener = mOnItemViewClickedListener
        }

    var adapter: ObjectAdapter?
        get() = mAdapter
        set(adapter) {
            mAdapter = adapter
            updateAdapter()
        }

    var onItemViewSelectedListener: OnItemViewSelectedListener?
        get() = mOnItemViewSelectedListener
        set(listener) {
            mOnItemViewSelectedListener = listener
        }

    var onItemViewClickedListener: OnItemViewClickedListener?
        get() = mOnItemViewClickedListener
        set(listener) {
            mOnItemViewClickedListener = listener
            mGridPresenter?.onItemViewClickedListener = mOnItemViewClickedListener
        }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mGridViewHolder = mGridPresenter?.onCreateViewHolder(browse_grid_dock)?.also {
            browse_grid_dock.addView(it.view)
            it.gridView.setOnChildLaidOutListener(mChildLaidOutListener)
        }
        mSceneAfterEntranceTransition = TransitionHelper.createScene(browse_grid_dock) {
            setEntranceTransitionState(true)
        }
        mGridViewHolder?.gridView?.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val cardDescriptionView = shadowDescriptionView?.getCardDescriptionView() ?: return@addOnLayoutChangeListener
            val newWidth = max(v.width - v.paddingLeft - v.paddingRight, cardDescriptionView.minimumWidth)
            val currentWidth = cardDescriptionView.layoutParams.width
            if (currentWidth != newWidth) {
                cardDescriptionView.updateLayoutParams {
                    width = newWidth
                }
            }
        }

        mainFragmentAdapter.fragmentHost.notifyViewCreated(mMainFragmentAdapter)
        updateAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mGridViewHolder = null
    }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return mMainFragmentAdapter
    }

    protected fun setDescription(title: CharSequence, subtitle: CharSequence) {
        shadowDescriptionView.setDescription(title, subtitle)
    }

    fun setSelectedPosition(position: Int) {
        val gridViewHolder = mGridViewHolder ?: return
        mSelectedPosition = position
        if (gridViewHolder.gridView.adapter != null) {
            gridViewHolder.gridView.setSelectedPositionSmooth(position)
        }
    }

    fun setEntranceTransitionState(afterTransition: Boolean) {
        mGridPresenter?.setEntranceTransitionState(mGridViewHolder, afterTransition)
    }

    private fun gridOnItemSelected(position: Int) {
        if (position != mSelectedPosition) {
            mSelectedPosition = position
            showOrHideTitle()
        }
    }

    private fun showOrHideTitle() {
        val gridViewHolder = mGridViewHolder ?: return
        if (gridViewHolder.gridView.findViewHolderForAdapterPosition(mSelectedPosition) == null) {
            return
        }
        if (!gridViewHolder.gridView.hasPreviousViewInSameRow(mSelectedPosition)) {
            mMainFragmentAdapter.fragmentHost.showTitleView(true)
        } else {
            mMainFragmentAdapter.fragmentHost.showTitleView(false)
        }
    }

    private fun updateAdapter() {
        val gridViewHolder = mGridViewHolder ?: return
        val gridPresenter = mGridPresenter ?: return
        gridPresenter.onBindViewHolder(mGridViewHolder, mAdapter)
        if (mSelectedPosition != -1) {
            gridViewHolder.gridView.selectedPosition = mSelectedPosition
        }
    }

}