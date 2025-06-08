package taiwa.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.LayoutRes

open class CustomBottomSheetDialogFragment(
    @LayoutRes contentLayoutId: Int = 0,
    @LayoutRes footerLayoutId: Int = 0,
) : BaseCustomDialogFragment(contentLayoutId, footerLayoutId) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return CustomBottomSheetDialog(requireContext(), theme)
    }
}