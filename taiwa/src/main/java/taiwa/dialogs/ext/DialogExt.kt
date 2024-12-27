package taiwa.dialogs.ext

import android.app.Dialog
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.ViewBindingProperty
import by.kirich1409.viewbindingdelegate.viewBinding
import taiwa.dialogs.BaseTaiwaDialogFragment

internal fun <T : Dialog> T.attachToLifecycle(
    lifecycleOwner: LifecycleOwner,
    ondDismiss: (() -> Unit)? = null,
): T {
    val dialog = this

    val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            dialog.dismiss()
        }
    }

    dialog.setOnDismissListener {
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        ondDismiss?.invoke()
    }

    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

    return dialog
}

inline fun <VB : ViewBinding> BaseTaiwaDialogFragment.footerViewBinding(
    crossinline vbFactory: (View) -> VB,
    noinline onViewDestroyed: (VB) -> Unit = {},
): ViewBindingProperty<BaseTaiwaDialogFragment, VB> {
    return viewBinding(
        vbFactory = vbFactory,
        viewProvider = { requireFooterView() },
        onViewDestroyed = onViewDestroyed
    )
}