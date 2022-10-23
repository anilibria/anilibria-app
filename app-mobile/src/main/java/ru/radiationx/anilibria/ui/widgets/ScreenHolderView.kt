package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_screenholder.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatDrawable
import timber.log.Timber

class ScreenHolderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    init {
        View.inflate(context, R.layout.view_screenholder, this)
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
        ivIcon.isVisible = icon != null
        ivIcon.setImageDrawable(icon)
    }

    fun setTitle(text: CharSequence?) {
        tvTitle.isVisible = text != null
        tvTitle.text = text
    }

    fun setSubtitle(text: CharSequence?) {
        tvSubtitle.isVisible = text != null
        tvSubtitle.text = text
    }

    fun setPrimaryButtonText(text: CharSequence?) {
        btPrimary.isVisible = text != null
        btPrimary.text = text
    }

    fun setSecondaryButtonText(text: CharSequence?) {
        btSecondary.isVisible = text != null
        btSecondary.text = text
    }

    fun setPrimaryButtonClickListener(onClick: () -> Unit) {
        btPrimary.setOnClickListener { onClick.invoke() }
    }

    fun setSecondaryClickListener(onClick: () -> Unit) {
        btSecondary.setOnClickListener { onClick.invoke() }
    }
}