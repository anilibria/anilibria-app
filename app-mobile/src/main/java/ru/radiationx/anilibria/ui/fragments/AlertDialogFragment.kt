package ru.radiationx.anilibria.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

open class AlertDialogFragment(@LayoutRes layoutRes: Int) : AppCompatDialogFragment(layoutRes) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).show()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
            )
        }
    }
}