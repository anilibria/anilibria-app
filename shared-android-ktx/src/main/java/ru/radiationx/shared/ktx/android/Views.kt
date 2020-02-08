package ru.radiationx.shared.ktx.android

import android.content.res.ColorStateList
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatDrawable


fun <T : View> T.visible() {
    this.visibility = View.VISIBLE
}

fun <T : View> T.gone() {
    this.visibility = View.GONE
}

fun <T : View> T.invisible() {
    this.visibility = View.INVISIBLE
}

fun <T : View> T.visible(isVisible: Boolean) = if (isVisible) visible() else gone()

fun <T : View> T.gone(isGone: Boolean) = visible(!isGone)

fun <T : View> T.invisible(isInvisible: Boolean) = if (isInvisible) invisible() else visible()

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
        LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

fun TextView.addTextChangeListener(action: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action.invoke(s?.toString().orEmpty())
        }
    })
}

fun ImageView.setCompatDrawable(@DrawableRes icRes: Int) = this.setImageDrawable(context.getCompatDrawable(icRes))

fun AppCompatImageView.setTint(color: Int) = ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
fun AppCompatImageView.setTintColor(@ColorRes colorRes: Int) = setTint(ContextCompat.getColor(context, colorRes))
fun AppCompatImageView.setTintColorAttr(@AttrRes colorAttr: Int) = setTint(context.getColorFromAttr(colorAttr))

fun AppCompatImageButton.setTint(color: Int) = ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
fun AppCompatImageButton.setTintColor(@ColorRes colorRes: Int) = setTint(ContextCompat.getColor(context, colorRes))
fun AppCompatImageButton.setTintColorAttr(@AttrRes colorAttr: Int) = setTint(context.getColorFromAttr(colorAttr))
