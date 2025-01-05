/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package taiwa.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.StyleRes
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.motion.MaterialBackOrchestrator
import taiwa.R
import taiwa.databinding.TaiwaBottomSheetDialogBinding
import kotlin.math.roundToInt

@SuppressLint("RestrictedApi")
class TaiwaBottomSheetDialog @JvmOverloads constructor(
    context: Context,
    @StyleRes theme: Int = 0,
) : BaseTaiwaDialog(context, getThemeResId(context, theme)) {

    private val binding by lazy {
        TaiwaBottomSheetDialogBinding.inflate(layoutInflater)
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                cancel()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private val behavior by lazy {
        BottomSheetBehavior.from(binding.contentWrapper).also {
            it.addBottomSheetCallback(bottomSheetCallback)
            it.isHideable = isCancelable()
        }
    }

    private val backOrchestrator by lazy {
        MaterialBackOrchestrator(behavior, binding.contentWrapper)
    }

    override val views: Views by lazy {
        Views(
            root = binding.root,
            transitionRoot = binding.container,
            contentContainer = binding.contentContainer,
            footerContainer = binding.footerContainer
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPeekHeight()

        binding.contentContainer.clipToOutline = true
        binding.footerContainer.clipToOutline = true

        binding.touchOutside.setOnClickListener {
            if (isNeedHandleTouchOutside()) {
                cancel()
            }
        }

        binding.contentContainer.setOnTouchListener { _, _ ->
            true
        }

        binding.footerContainer.setOnTouchListener { _, _ ->
            true
        }
    }

    override fun onStart() {
        super.onStart()
        if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateListeningForBackCallbacks()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        backOrchestrator.stopListeningForBackCallbacks()
    }

    override fun setFooterVisible(value: Boolean) {
        binding.footerContainer.isVisible = value
    }

    override fun prepareContentView() {
        val container = binding.contentContainer
        if (container.childCount > 1) {
            container.removeViews(1, container.childCount - 1)
        }
    }

    override fun prepareFooterView() {
        binding.footerContainer.removeAllViews()
    }

    override fun applyWrapperInsets(wrapperInsets: Insets) {
        binding.container.updatePadding(
            top = wrapperInsets.top,
        )
        binding.coordinator.updateLayoutParams<MarginLayoutParams> {
            leftMargin = wrapperInsets.left
            rightMargin = wrapperInsets.right
        }
        binding.footerWrapper.updateLayoutParams<MarginLayoutParams> {
            leftMargin = wrapperInsets.left
            rightMargin = wrapperInsets.right
            bottomMargin = wrapperInsets.bottom
        }
    }

    private fun initPeekHeight() {
        binding.contentWrapper.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            behavior.peekHeight = calculatePeekHeight()
        }
    }

    private fun calculatePeekHeight(): Int {
        val desiredHeight = (binding.coordinator.height * 0.8f).roundToInt()
        val sheetHeight = binding.contentWrapper.height
        val prevPeekHeight = behavior.peekHeight
        return if (desiredHeight == 0 || sheetHeight == 0) {
            if (prevPeekHeight == 0) {
                BottomSheetBehavior.PEEK_HEIGHT_AUTO
            } else {
                prevPeekHeight
            }
        } else {
            minOf(desiredHeight, sheetHeight)
        }
    }

    private fun updateListeningForBackCallbacks() {
        if (isCancelable()) {
            backOrchestrator.startListeningForBackCallbacks()
        } else {
            backOrchestrator.stopListeningForBackCallbacks()
        }
    }

    companion object {
        private fun getThemeResId(context: Context, themeId: Int): Int {
            if (themeId != 0) return themeId
            val outValue = TypedValue().let {
                val result = context.theme.resolveAttribute(R.attr.taiwaBottomSheetTheme, it, true)
                it.takeIf { result }
            }
            return outValue?.resourceId ?: R.style.ThemeOverlay_Taiwa_BottomSheetDialog
        }
    }
}
