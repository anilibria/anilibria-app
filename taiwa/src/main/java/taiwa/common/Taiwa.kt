package taiwa.common

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.TaiwaEvent
import taiwa.dsl.TaiwaContentScope
import taiwa.internal.buildTaiwa
import taiwa.internal.models.TaiwaContentState
import taiwa.internal.view.TaiwaView
import java.lang.ref.WeakReference

class Taiwa(
    parentContext: Context,
    lifecycleOwner: LifecycleOwner,
    type: DialogType,
) {
    private val dialogWrapper = DialogWrapper(parentContext, lifecycleOwner, type)

    private var eventListener: ((TaiwaEvent) -> Unit)? = null

    private var currentView: WeakReference<TaiwaView>? = null

    private var currentContent: TaiwaContentState? = null

    init {
        dialogWrapper.setCloseListener {
            currentContent?.closeListener?.invoke()
            eventListener?.invoke(TaiwaEvent.Close)
        }
        dialogWrapper.setBackListener {
            currentContent?.header?.backAction?.also {
                handleAction(it)
            }
        }
        getContentView().actionListener = { action ->
            handleAction(action)
        }
    }

    private fun getContentView(): TaiwaView {
        val view = currentView?.get() ?: dialogWrapper.setContentView {
            TaiwaView(it)
        }
        currentView = WeakReference(view)
        return view
    }

    internal fun setContentState(content: TaiwaContentState) {
        if (currentContent == content) {
            return
        }
        currentContent = content
        dialogWrapper.setBackListenerEnabled(content.header?.backAction != null)
        val transition = dialogWrapper.prepareTransition()
        getContentView().setState(content, transition) {
            dialogWrapper.beginViewTransition(transition)
        }
    }

    fun setContent(block: TaiwaContentScope.() -> Unit) {
        setContentState(buildTaiwa(block))
    }

    fun setEventListener(listener: (TaiwaEvent) -> Unit) {
        eventListener = listener
    }

    fun show() {
        dialogWrapper.show()
    }

    fun close() {
        dialogWrapper.close()
    }

    private fun handleAction(action: TaiwaAction) {
        when (action) {
            TaiwaAction.Close -> dialogWrapper.close()
            TaiwaAction.Root -> eventListener?.invoke(TaiwaEvent.Anchor(TaiwaAnchor.Root))
            is TaiwaAction.Anchor -> eventListener?.invoke(TaiwaEvent.Anchor(action.anchor))
        }
    }
}