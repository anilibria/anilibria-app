package taiwa.internal.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.button.MaterialButton
import taiwa.TaiwaAction
import taiwa.common.ViewTransition
import taiwa.databinding.TaiwaRootBinding
import taiwa.internal.models.ClickListener
import taiwa.internal.models.TaiwaButtonState
import taiwa.internal.models.TaiwaButtonsState
import taiwa.internal.models.TaiwaContentState
import taiwa.internal.models.TaiwaHeaderState
import taiwa.internal.models.TaiwaItemsState
import taiwa.internal.models.TaiwaMessageState

internal class TaiwaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by viewBinding<TaiwaRootBinding>(attachToRoot = true)

    private val itemsAdapter by lazy {
        TaiwaItemsAdapter {
            handleClick(it.base.action, it.base.clickListener)
        }
    }

    private val customAnimator = DefaultItemAnimator().apply {
        supportsChangeAnimations = false
    }

    var actionListener: ((TaiwaAction) -> Unit)? = null

    init {
        binding.itemsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            itemAnimator = customAnimator
        }
        binding.headerClose.setOnClickListener {
            handleClick(TaiwaAction.Close, null)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.itemsRecycler.adapter = itemsAdapter
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.itemsRecycler.adapter = null
    }

    fun setState(
        content: TaiwaContentState,
        transition: ViewTransition,
        onCommit: () -> Unit = {},
    ) {
        updateAnimator(transition)
        setState(content.items) {
            onCommit()
            content.header.also(::setState)
            content.message.also(::setState)
            content.buttons.also(::setState)
            updateSpaces()
        }
    }

    private fun updateAnimator(transition: ViewTransition) {
        val selectedAnimator = customAnimator.takeIf { transition.prepared }
        val currentAnimator = binding.itemsRecycler.itemAnimator
        if (selectedAnimator == currentAnimator) return
        binding.itemsRecycler.itemAnimator = selectedAnimator
    }

    private fun updateSpaces() {
        val hasHeader = binding.header.isVisible
        val hasMessage = binding.message.isVisible
        val hasItems = binding.itemsRecycler.isVisible
        val hasButtons = binding.buttonsContainer.isVisible
        binding.spaceTop.isVisible = hasItems && hasMessage.not() && hasHeader.not()
        binding.spaceBottom.isVisible = hasItems && hasButtons.not()
    }

    private fun setState(header: TaiwaHeaderState?) {
        binding.header.isVisible = header != null
        if (header == null) return
        binding.headerTitle.setStateText(header.title)
        binding.headerSubtitle.setStateText(header.subtitle)
        binding.headerBack.isVisible = header.backAction != null
        binding.headerClose.isVisible = header.canClose
        binding.headerBack.setOnClickListener {
            handleClick(header.backAction, null)
        }
    }

    private fun setState(message: TaiwaMessageState?) {
        binding.message.setStateText(message?.text)
    }

    private fun setState(items: TaiwaItemsState?, callback: () -> Unit) {
        binding.itemsRecycler.isVisible = items != null
        if (items == null) {
            callback.invoke()
            return
        }
        itemsAdapter.submitList(items.items, callback)
    }

    private fun setState(buttons: TaiwaButtonsState?) {
        binding.buttonsContainer.isVisible = buttons != null
        if (buttons == null) return
        binding.buttonsContainer.removeAllViews()
        buttons.buttons.forEach { button ->
            setState(button)
        }
    }

    private fun setState(button: TaiwaButtonState) {
        val buttonView = MaterialButton(binding.buttonsContainer.context)
        buttonView.tag = button.id
        buttonView.text = button.text
        buttonView.setOnClickListener {
            handleClick(button.action, button.clickListener)
        }
        binding.buttonsContainer.addView(buttonView)
    }

    private fun handleClick(action: TaiwaAction?, clickListener: ClickListener?) {
        clickListener?.invoke()
        if (action != null) {
            actionListener?.invoke(action)
        }
    }
}