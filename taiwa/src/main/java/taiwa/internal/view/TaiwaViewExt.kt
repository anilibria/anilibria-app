package taiwa.internal.view

import android.widget.TextView
import androidx.core.view.isVisible

fun TextView.setStateText(stateText: String?) {
    isVisible = stateText != null
    if (stateText != null) {
        text = stateText
    }
}