package taiwa.internal.view

import envoy.ext.viewBindingEnvoy
import taiwa.databinding.TaiwaMessageBinding
import taiwa.internal.models.TaiwaMessageState

internal fun messageEnvoy() = viewBindingEnvoy<TaiwaMessageState, TaiwaMessageBinding> {

    bind { message ->
        view.root.text = message.text
    }
}