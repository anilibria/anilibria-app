package ru.radiationx.shared.ktx.android

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat

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

fun View.setBackgroundTint(color: Int) =
    ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(color))

fun View.setBackgroundTintRes(@ColorRes resId: Int) =
    setBackgroundTint(getCompatColor(resId))

fun View.setBackgroundTintAttr(@AttrRes resId: Int) =
    setBackgroundTint(context.getColorFromAttr(resId))

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
    visibilityAction: (Boolean) -> Unit,
) {
    visibilityAction.invoke(this != null)
    if (this != null) {
        bindAction.invoke(this)
    }
}

fun <T> T?.bindOptionalView(
    view: View,
    bindAction: (T) -> Unit,
) = bindOptional(bindAction) { visible ->
    view.isVisible = visible
}

fun <T> T?.bindOptionalViews(
    views: Iterable<View>,
    bindAction: (T) -> Unit,
) = bindOptional(bindAction) { visible ->
    views.forEach {
        it.isVisible = visible
    }
}
