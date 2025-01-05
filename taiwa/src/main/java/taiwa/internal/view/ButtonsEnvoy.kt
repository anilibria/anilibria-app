package taiwa.internal.view

import com.google.android.material.button.MaterialButton
import envoy.ext.viewBindingEnvoy
import taiwa.R
import taiwa.TaiwaAction
import taiwa.databinding.TaiwaButtonsBinding
import taiwa.internal.models.ClickListener
import taiwa.internal.models.TaiwaButtonState
import taiwa.internal.models.TaiwaButtonsState

internal fun buttonsEnvoy(
    clickListener: (TaiwaButtonState) -> Unit
) = viewBindingEnvoy<TaiwaButtonsState, TaiwaButtonsBinding> {

    fun addButton(button: TaiwaButtonState) {
        val buttonView = MaterialButton(view.root.context)
        buttonView.tag = button.id
        buttonView.text = button.text
        buttonView.setOnClickListener {
            clickListener.invoke(button)
        }
        view.buttonsContainer.addView(buttonView)
    }

    bind { buttons ->
        view.buttonsContainer.removeAllViews()
        buttons.buttons.forEach { button ->
            addButton(button)
        }
    }
}