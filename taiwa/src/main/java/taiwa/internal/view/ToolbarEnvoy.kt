package taiwa.internal.view

import androidx.core.view.isVisible
import envoy.ext.viewBindingEnvoy
import taiwa.databinding.TaiwaToolbarBinding
import taiwa.internal.models.TaiwaToolbarState

internal fun toolbarEnvoy(
    backClickListener: (TaiwaToolbarState) -> Unit,
    closeClickListener: () -> Unit
) = viewBindingEnvoy<TaiwaToolbarState, TaiwaToolbarBinding> {

    view.headerClose.setOnClickListener {
        closeClickListener.invoke()
    }
    bind { header ->
        view.headerTitle.setStateText(header.title)
        view.headerSubtitle.setStateText(header.subtitle)
        view.headerBack.isVisible = header.withBack
        view.headerClose.isVisible = header.withClose
        view.headerBack.setOnClickListener {
            backClickListener.invoke(header)
        }
    }
}