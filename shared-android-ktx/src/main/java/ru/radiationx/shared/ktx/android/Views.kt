package ru.radiationx.shared.ktx.android

import android.content.res.ColorStateList
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible

@Deprecated("Use androidx pls")
fun <T : View> T.visible() {
    this.visibility = View.VISIBLE
}

@Deprecated("Use androidx pls")
fun <T : View> T.gone() {
    this.visibility = View.GONE
}

@Deprecated("Use androidx pls")
fun <T : View> T.invisible() {
    this.visibility = View.INVISIBLE
}

@Deprecated("Use androidx pls")
fun <T : View> T.visible(isVisible: Boolean) = if (isVisible) visible() else gone()

@Deprecated("Use androidx pls")
fun <T : View> T.gone(isGone: Boolean) = visible(!isGone)

@Deprecated("Use androidx pls")
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

fun ImageView.setCompatDrawable(@DrawableRes icRes: Int) =
    this.setImageDrawable(context.getCompatDrawable(icRes))

fun AppCompatImageView.setTint(color: Int) =
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))

fun AppCompatImageView.setTintColor(@ColorRes colorRes: Int) =
    setTint(ContextCompat.getColor(context, colorRes))

fun AppCompatImageView.setTintColorAttr(@AttrRes colorAttr: Int) =
    setTint(context.getColorFromAttr(colorAttr))

fun AppCompatImageButton.setTint(color: Int) =
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))

fun AppCompatImageButton.setTintColor(@ColorRes colorRes: Int) =
    setTint(ContextCompat.getColor(context, colorRes))

fun AppCompatImageButton.setTintColorAttr(@AttrRes colorAttr: Int) =
    setTint(context.getColorFromAttr(colorAttr))

fun <T> T?.bindOptional(
    bindAction: (T) -> Unit,
    visibilityAction: (Boolean) -> Unit
) {
    visibilityAction.invoke(this != null)
    if (this != null) {
        bindAction.invoke(this)
    }
}

fun <T> T?.bindOptionalView(
    view: View,
    bindAction: (T) -> Unit
) = bindOptional(bindAction, { visible ->
    view.isVisible = visible
})

fun <T> T?.bindOptionalViews(
    views: Iterable<View>,
    bindAction: (T) -> Unit
) = bindOptional(bindAction, { visible ->
    views.forEach {
        it.isVisible = visible
    }
})
