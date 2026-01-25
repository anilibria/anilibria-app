package taiwa.common

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import envoy.Envoy
import envoy.DiffItem
import taiwa.TaiwaAnchor
import taiwa.TaiwaEvent
import taiwa.dsl.TaiwaNestingScope
import taiwa.internal.buildNestingTaiwa
import taiwa.internal.view.NestedContentController

class NestedTaiwa(
    parentContext: Context,
    lifecycleOwner: LifecycleOwner,
    type: DialogType,
) {

    private val taiwa = Taiwa(parentContext, lifecycleOwner, type)

    private var closeListener: (() -> Unit)? = null

    private val contentController = NestedContentController()

    init {
        contentController.currentStateFlow.onEach {
            taiwa.setContentState(it)
        }.launchIn(lifecycleOwner.lifecycleScope)
        taiwa.setEventListener { event ->
            when (event) {
                TaiwaEvent.Close -> {
                    closeListener?.invoke()
                    contentController.toAnchor(TaiwaAnchor.Root)
                }

                is TaiwaEvent.Anchor -> contentController.toAnchor(event.anchor)
            }
        }
    }

    fun setContent(block: TaiwaNestingScope.() -> Unit) {
        contentController.apply(buildNestingTaiwa(block))
    }

    fun addDelegate(delegate: Envoy<DiffItem>) {
        taiwa.addDelegate(delegate)
    }

    fun show() {
        taiwa.show()
    }

    fun close() {
        taiwa.close()
    }
}