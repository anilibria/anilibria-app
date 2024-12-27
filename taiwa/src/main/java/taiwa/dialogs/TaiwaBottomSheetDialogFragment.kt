package taiwa.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.LayoutRes

open class TaiwaBottomSheetDialogFragment(
    @LayoutRes contentLayoutId: Int = 0,
    @LayoutRes footerLayoutId: Int = 0,
) : BaseTaiwaDialogFragment(contentLayoutId, footerLayoutId) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TaiwaBottomSheetDialog(requireContext(), theme)
    }
}