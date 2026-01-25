package taiwa.dialogs

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.StyleRes
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import taiwa.R
import taiwa.databinding.TaiwaAlertDialogBinding

class TaiwaDialog @JvmOverloads constructor(
    context: Context,
    @StyleRes theme: Int = 0,
) : BaseTaiwaDialog(context, getThemeResId(context, theme)) {

    private val binding by lazy {
        TaiwaAlertDialogBinding.inflate(layoutInflater)
    }

    override val views: Views by lazy {
        Views(
            root = binding.root,
            transitionRoot = binding.contentWrapper,
            contentContainer = binding.contentContainer,
            footerContainer = binding.footerContainer
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.commonContainer.clipToOutline = true
        binding.touchOutside.setOnClickListener {
            if (isNeedHandleTouchOutside()) {
                cancel()
            }
        }
        binding.commonContainer.setOnTouchListener { _, _ ->
            true
        }
    }

    override fun setFooterVisible(value: Boolean) {
        binding.footerContainer.isVisible = value
    }

    override fun prepareContentView() {
        binding.contentContainer.removeAllViews()
    }

    override fun prepareFooterView() {
        binding.footerContainer.removeAllViews()
    }

    override fun applyWrapperInsets(wrapperInsets: Insets) {
        binding.contentWrapper.updatePadding(
            top = wrapperInsets.top,
            bottom = wrapperInsets.bottom
        )
        binding.contentWrapper.updateLayoutParams<MarginLayoutParams> {
            leftMargin = wrapperInsets.left
            rightMargin = wrapperInsets.right
        }
    }

    companion object {
        private fun getThemeResId(context: Context, themeId: Int): Int {
            if (themeId != 0) return themeId
            val outValue = TypedValue().let {
                val result = context.theme.resolveAttribute(R.attr.taiwaAlertTheme, it, true)
                it.takeIf { result }
            }
            return outValue?.resourceId ?: R.style.ThemeOverlay_Taiwa_Dialog
        }
    }
}