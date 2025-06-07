package taiwa.internal.view

import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import envoy.ext.viewBindingEnvoy
import envoy.recycler.DiffItemEnvoyAdapter
import taiwa.databinding.TaiwaChipsBinding
import taiwa.internal.models.TaiwaChipState
import taiwa.internal.models.TaiwaChipsState


internal fun chipsEnvoy(
    clickListener: (TaiwaChipState) -> Unit
) = viewBindingEnvoy<TaiwaChipsState, TaiwaChipsBinding> {

    val itemsAdapter by lazy {
        DiffItemEnvoyAdapter().apply {
            addEnvoy(chipItemEnvoy(clickListener))
        }
    }

    attach {
        view.root.adapter = itemsAdapter
        view.root.layoutManager = FlexboxLayoutManager(view.root.context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
        view.root.itemAnimator = null
    }

    bind { chips ->
        itemsAdapter.setItems(chips.chips)
    }

    detach {
        view.root.adapter = null
        view.root.layoutManager = null
    }
}