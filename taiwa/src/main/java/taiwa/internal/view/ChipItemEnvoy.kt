package taiwa.internal.view

import envoy.ext.viewBindingEnvoy
import taiwa.databinding.TaiwaChipItemBinding
import taiwa.internal.models.TaiwaChipState

internal fun chipItemEnvoy(
    clickListener: (TaiwaChipState) -> Unit
) = viewBindingEnvoy<TaiwaChipState, TaiwaChipItemBinding> {

    bind { chip ->
        view.root.text = chip.text
        view.root.setOnClickListener {
            clickListener.invoke(chip)
        }
    }
}