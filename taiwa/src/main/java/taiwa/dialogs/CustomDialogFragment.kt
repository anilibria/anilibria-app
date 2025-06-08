package taiwa.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.LayoutRes

open class CustomDialogFragment(
    @LayoutRes contentLayoutId: Int = 0,
    @LayoutRes footerLayoutId: Int = 0,
) : BaseCustomDialogFragment(contentLayoutId, footerLayoutId) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return CustomDialog(requireContext(), theme)
    }
}