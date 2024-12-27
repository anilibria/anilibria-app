package taiwa.common

import android.app.Dialog
import android.view.View

class ViewTransition internal constructor(
    view: View,
    dialog: Dialog,
) {
    val prepared = view.isLaidOut && view.windowId != null && dialog.isShowing
}