package ru.radiationx.anilibria.common

import android.app.AlertDialog
import androidx.activity.ComponentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.mintrocket.lib.mintpermissions.flows.models.DialogRequestContent
import ru.mintrocket.lib.mintpermissions.flows.models.DialogResult
import ru.mintrocket.lib.mintpermissions.flows.ui.DialogContentConsumer
import ru.radiationx.shared.ktx.android.showWithLifecycle
import kotlin.coroutines.resume

class LeanbackDialogContentConsumer : DialogContentConsumer {
    override suspend fun request(
        activity: ComponentActivity,
        request: DialogRequestContent
    ): DialogResult {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(activity)
                .setTitle(request.content.title)
                .setMessage(request.content.message)
                .setPositiveButton(request.content.actionBtn) { _, _ ->
                    continuation.resume(DialogResult.ACTION)
                }
                .setNegativeButton(request.content.cancelBtn) { _, _ ->
                    continuation.resume(DialogResult.CANCEL)
                }
                .setOnCancelListener { continuation.resume(DialogResult.CANCEL) }
                .showWithLifecycle(activity)
            continuation.invokeOnCancellation {
                dialog.dismiss()
            }
        }
    }
}