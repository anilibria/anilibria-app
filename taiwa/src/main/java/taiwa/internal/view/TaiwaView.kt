package taiwa.internal.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import dev.androidbroadcast.vbpd.viewBinding
import envoy.DiffItem
import envoy.Envoy
import envoy.recycler.DiffItemEnvoyAdapter
import taiwa.TaiwaAction
import taiwa.common.ViewTransition
import taiwa.databinding.TaiwaRootBinding
import taiwa.internal.models.ClickListener
import taiwa.internal.models.TaiwaDividerState
import taiwa.internal.models.TaiwaItem

internal class TaiwaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by viewBinding<TaiwaRootBinding>(attachToRoot = true)

    private val itemsAdapter by lazy {
        DiffItemEnvoyAdapter().apply {
            addEnvoy(itemEnvoy {
                handleClick(it.base.action, it.base.clickListener)
            })
            addEnvoy(
                toolbarEnvoy(
                    backClickListener = {
                        backListener?.invoke()
                    },
                    closeClickListener = {
                        handleClick(TaiwaAction.Close, null)
                    }
                )
            )
            addEnvoy(messageEnvoy())
            addEnvoy(sectionEnvoy())
            addEnvoy(dividerEnvoy())
            addEnvoy(buttonsEnvoy {
                handleClick(it.action, it.clickListener)
            })
            addEnvoy(chipsEnvoy {
                handleClick(it.action, it.clickListener)
            })
        }
    }

    private val customAnimator = DefaultItemAnimator().apply {
        supportsChangeAnimations = false
    }

    var actionListener: ((TaiwaAction) -> Unit)? = null

    var backListener: (() -> Unit)? = null

    init {
        binding.itemsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = customAnimator
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

    fun addDelegate(delegate: Envoy<DiffItem>) {
        itemsAdapter.addEnvoy(delegate)
    }

    fun setItems(
        items: List<DiffItem>,
        transition: ViewTransition,
    ) {
        val filterItems = items.toMutableList().apply {
            if (firstOrNull() is TaiwaDividerState) {
                removeAt(0)
            }
            if (lastOrNull() is TaiwaDividerState) {
                removeAt(lastIndex)
            }
        }
        updateAnimator(transition)
        updateSpaces(filterItems)
        setItems(filterItems)
    }

    private fun setItems(items: List<DiffItem>) {
        binding.itemsRecycler.isVisible = items.isNotEmpty()
        itemsAdapter.setItems(items)
    }

    private fun updateAnimator(transition: ViewTransition) {
        val selectedAnimator = customAnimator.takeIf { transition.prepared }
        val currentAnimator = binding.itemsRecycler.itemAnimator
        if (selectedAnimator == currentAnimator) return
        binding.itemsRecycler.itemAnimator = selectedAnimator
    }

    private fun updateSpaces(items: List<DiffItem>) {
        binding.spaceTop.isVisible = items.firstOrNull() is TaiwaItem
        binding.spaceBottom.isVisible = items.lastOrNull() is TaiwaItem
    }

    private fun handleClick(action: TaiwaAction?, clickListener: ClickListener?) {
        Log.e("kekeke", "handleClick $action, $clickListener")
        clickListener?.invoke()
        if (action != null) {
            actionListener?.invoke(action)
        }
    }
}