package taiwa.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import taiwa.dialogs.BaseTaiwaDialog
import taiwa.dialogs.TaiwaDialog
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import taiwa.dialogs.TaiwaBottomSheetDialog
import taiwa.dialogs.ext.attachToLifecycle

class DialogWrapper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val type: DialogType,
) {

    private val dialog: BaseTaiwaDialog = when (type) {
        DialogType.Alert -> TaiwaDialog(context)
        DialogType.BottomSheet -> TaiwaBottomSheetDialog(context)
    }.attachToLifecycle(lifecycleOwner)

    private val backPressedCallback = dialog.onBackPressedDispatcher.addCallback(enabled = false) {
        backListener?.invoke()
    }

    private var closeListener: (() -> Unit)? = null

    private var backListener: (() -> Unit)? = null

    init {
        dialog.setOnCancelListener {
            closeListener?.invoke()
        }
    }

    fun setCloseListener(listener: () -> Unit) {
        closeListener = listener
    }

    fun setBackListener(listener: () -> Unit) {
        backListener = listener
    }

    fun setBackListenerEnabled(value: Boolean) {
        backPressedCallback.isEnabled = value
    }

    fun show() {
        dialog.show()
    }

    fun close() {
        dialog.cancel()
    }

    fun <T : View> setContentView(block: ((context: Context) -> T)): T {
        return dialog.setContentView(block)
    }

    fun <T : ViewBinding> setContentBinding(block: ((inflater: LayoutInflater) -> T)): T {
        return dialog.setContentBinding(block)
    }

    fun <T : View> setFooterView(block: ((context: Context) -> T)): T {
        return dialog.setFooterView(block)
    }

    fun <T : ViewBinding> setFooterBinding(block: ((inflater: LayoutInflater) -> T)): T {
        return dialog.setFooterBinding(block)
    }

    fun prepareTransition(): ViewTransition {
        return dialog.prepareViewTransition()
    }

    fun beginViewTransition(transition: ViewTransition) {
        dialog.beginViewTransition(transition)
    }
}