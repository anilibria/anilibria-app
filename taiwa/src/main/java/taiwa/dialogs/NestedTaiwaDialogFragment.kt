package taiwa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import taiwa.common.DialogType
import taiwa.common.NestedTaiwa
import taiwa.lifecycle.lifecycleLazy

open class NestedTaiwaDialogFragment(
    private val type: DialogType,
) : BaseCustomDialogFragment() {

    protected val nestedTaiwa by lifecycleLazy {
        NestedTaiwa(requireContext(), viewLifecycleOwner, type) {
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
        return nestedTaiwa.taiwa.getContentView()
    }

    final override fun onCreateFooterView(
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        return nestedTaiwa.taiwa.getFooterView()
    }
}