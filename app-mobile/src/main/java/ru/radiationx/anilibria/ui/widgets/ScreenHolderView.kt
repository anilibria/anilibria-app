package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ViewScreenholderBinding
import ru.radiationx.shared.ktx.android.getCompatDrawable
import timber.log.Timber

class ScreenHolderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding by viewBinding<ViewScreenholderBinding>(attachToRoot = true)

    init {
        initAttributes(context, attrs)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        attrs ?: return
        val array = context.obtainStyledAttributes(attrs, R.styleable.ScreenHolderView)
        try {
            val iconRes = array.getResourceId(R.styleable.ScreenHolderView_shvIcon, -1)
            if (iconRes != -1) {
                setIcon(context.getCompatDrawable(iconRes))
            }
            setTitle(array.getText(R.styleable.ScreenHolderView_shvTitle))
            setSubtitle(array.getText(R.styleable.ScreenHolderView_shvSubtitle))
            setPrimaryButtonText(array.getText(R.styleable.ScreenHolderView_shvPrimaryButton))
            setSecondaryButtonText(array.getText(R.styleable.ScreenHolderView_shvSecondaryButton))
        } catch (ex: Exception) {
            Timber.e(ex)
        } finally {
            array.recycle()
        }
    }

    fun setIcon(icon: Drawable?) {
        binding.ivIcon.isVisible = icon != null
        binding.ivIcon.setImageDrawable(icon)
    }

    fun setTitle(text: CharSequence?) {
        binding.tvTitle.isVisible = text != null
        binding.tvTitle.text = text
    }

    fun setSubtitle(text: CharSequence?) {
        binding.tvSubtitle.isVisible = text != null
        binding.tvSubtitle.text = text
    }

    fun setPrimaryButtonText(text: CharSequence?) {
        binding.btPrimary.isVisible = text != null
        binding.btPrimary.text = text
    }

    fun setSecondaryButtonText(text: CharSequence?) {
        binding.btSecondary.isVisible = text != null
        binding.btSecondary.text = text
    }

    fun setPrimaryButtonClickListener(onClick: () -> Unit) {
        binding.btPrimary.setOnClickListener { onClick.invoke() }
    }

    fun setSecondaryClickListener(onClick: () -> Unit) {
        binding.btSecondary.setOnClickListener { onClick.invoke() }
    }
}