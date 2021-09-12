package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_screenholder.view.*
import ru.radiationx.anilibria.R

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
            setIcon(array.getDrawable(R.styleable.ScreenHolderView_shvIcon))
            setTitle(array.getText(R.styleable.ScreenHolderView_shvTitle))
            setSubtitle(array.getText(R.styleable.ScreenHolderView_shvSubtitle))
            setPrimaryButtonText(array.getText(R.styleable.ScreenHolderView_shvPrimaryButton))
            setSecondaryButtonText(array.getText(R.styleable.ScreenHolderView_shvSecondaryButton))
        } catch (ex: Exception) {
            ex.printStackTrace()
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