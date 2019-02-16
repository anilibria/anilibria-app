package ru.radiationx.anilibria.extension

import android.support.annotation.LayoutRes
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


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