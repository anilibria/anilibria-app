package taiwa.internal.view

import envoy.ext.viewBindingEnvoy
import taiwa.databinding.TaiwaSectionBinding
import taiwa.internal.models.TaiwaSectionState

internal fun sectionEnvoy() = viewBindingEnvoy<TaiwaSectionState, TaiwaSectionBinding> {

    bind { message ->
        view.root.text = message.text
    }
}