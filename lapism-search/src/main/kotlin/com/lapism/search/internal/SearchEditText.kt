package com.lapism.search.internal

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.annotation.AttrRes
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class SearchEditText : AppCompatEditText {

    // *********************************************************************************************
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // *********************************************************************************************
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            val imeVisible = ViewCompat.getRootWindowInsets(this)
                ?.isVisible(WindowInsetsCompat.Type.ime())
                ?: false
            if (hasFocus() && (!imeVisible || text?.isEmpty() == true)) {
                clearFocus()
                clearText()
                return true
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    fun clearText() {
        text?.clear()
    }

}
