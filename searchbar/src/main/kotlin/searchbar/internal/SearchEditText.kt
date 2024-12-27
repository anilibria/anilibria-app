package searchbar.internal

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            val imeVisible = ViewCompat.getRootWindowInsets(this)
                ?.isVisible(WindowInsetsCompat.Type.ime())
                ?: false
            if (hasFocus() && (!imeVisible || isTextEmpty())) {
                clearFocus()
                clearText()
                return true
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    fun isTextEmpty(): Boolean {
        return TextUtils.isEmpty(text)
    }

    fun clearText() {
        text?.clear()
    }

    fun getQuery(): String {
        return text?.toString().orEmpty()
    }

}
