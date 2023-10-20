package ru.radiationx.shared.ktx.android

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun <T : Dialog> T.showWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    ondDismiss: (() -> Unit)? = null,
): T {
    val dialog = this

    val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            if (!dialog.isShowing) return
            dialog.dismiss()
        }
    }

    dialog.setOnDismissListener {
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        ondDismiss?.invoke()
    }

    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

    dialog.show()

    return dialog
}

fun AlertDialog.Builder.showWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    ondDismiss: (() -> Unit)? = null,
): AlertDialog {
    val dialog = create()
    dialog.showWithLifecycle(lifecycleOwner, ondDismiss)
    return dialog
}

fun android.app.AlertDialog.Builder.showWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    ondDismiss: (() -> Unit)? = null,
): android.app.AlertDialog {
    val dialog = create()
    dialog.showWithLifecycle(lifecycleOwner, ondDismiss)
    return dialog
}