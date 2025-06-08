package taiwa.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

open class BaseCustomDialogFragment(
    @LayoutRes contentLayoutId: Int = 0,
    @LayoutRes private val footerLayoutId: Int = 0,
) : AppCompatDialogFragment(contentLayoutId) {

    private var mFooterView: View? = null

    private val mObserver = Observer<LifecycleOwner?> { value ->
        if (footerLayoutId != 0 && value != null && showsDialog) {
            val footerView = requireFooterView()
            val customDialog = requireDialog()
            require(customDialog is BaseCustomDialog) {
                "Footer view can't set in this dialog $dialog"
            }
            customDialog.setFooterView(footerView)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewLifecycleOwnerLiveData.observeForever(mObserver)
    }

    override fun onDetach() {
        super.onDetach()
        viewLifecycleOwnerLiveData.removeObserver(mObserver)
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val contentView = onCreateContentView(inflater, container, savedInstanceState)
        if (contentView == null && footerLayoutId != 0) {
            error("Can't create footerView without contentView")
        }
        mFooterView = onCreateFooterView(inflater, savedInstanceState)
        return contentView
    }

    open fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    open fun onCreateFooterView(
        inflater: LayoutInflater,
        savedInstanceState: Bundle?,
    ): View? {
        if (footerLayoutId != 0) {
            return inflater.inflate(footerLayoutId, null, false)
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val footerView = mFooterView
        if (footerView != null) {
            (footerView.parent as? ViewGroup?)?.removeView(footerView)
        }
        mFooterView = null
    }

    fun getFooterView(): View? {
        return mFooterView
    }

    fun requireFooterView(): View {
        return requireNotNull(getFooterView()) {
            "Fragment $this did not return a View from onCreateView() or this was called before onCreateView()."
        }
    }
}