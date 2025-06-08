package taiwa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import taiwa.common.DialogType
import taiwa.common.Taiwa
import taiwa.lifecycle.lifecycleLazy

open class TaiwaDialogFragment(
    private val type: DialogType,
) : BaseCustomDialogFragment() {

    protected val taiwa by lifecycleLazy {
        Taiwa(requireContext(), viewLifecycleOwner, type) {
            requireDialog() as BaseCustomDialog
        }
    }

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = when (type) {
        DialogType.Alert -> CustomDialog(requireContext(), theme)
        DialogType.BottomSheet -> CustomBottomSheetDialog(requireContext(), theme)
    }

    final override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return taiwa.getContentView()
    }

    final override fun onCreateFooterView(
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        return taiwa.getFooterView()
    }
}