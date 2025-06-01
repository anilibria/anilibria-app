package taiwa.common

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import envoy.DiffItem
import envoy.Envoy
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.TaiwaEvent
import taiwa.dsl.TaiwaScope
import taiwa.internal.buildTaiwa
import taiwa.internal.models.TaiwaState
import taiwa.internal.view.TaiwaView
import java.lang.ref.WeakReference

class Taiwa(
    parentContext: Context,
    lifecycleOwner: LifecycleOwner,
    type: DialogType,
) {
    private val dialogWrapper = DialogWrapper(parentContext, lifecycleOwner, type)

    private var eventListener: ((TaiwaEvent) -> Unit)? = null

    private var currentContentView: WeakReference<TaiwaView>? = null
    private var currentFooterView: WeakReference<TaiwaView>? = null

    private var currentContent: TaiwaState? = null

    init {
        dialogWrapper.setCloseListener {
            currentContent?.closeListener?.invoke()
            eventListener?.invoke(TaiwaEvent.Close)
        }
        dialogWrapper.setBackListener {
            findBackAction()?.also { handleAction(it) }
        }
        getContentView().apply {
            actionListener = { action ->
                handleAction(action)
            }
            backListener = {
                findBackAction()?.also { handleAction(it) }
            }
        }
        getFooterView().apply {
            actionListener = { action ->
                handleAction(action)
            }
            backListener = {
                findBackAction()?.also { handleAction(it) }
            }
        }
    }

    private fun getContentView(): TaiwaView {
        val view = currentContentView?.get() ?: dialogWrapper.setContentView { TaiwaView(it) }
        currentContentView = WeakReference(view)
        return view
    }

    private fun getFooterView(): TaiwaView {
        val view = currentFooterView?.get() ?: dialogWrapper.setFooterView { TaiwaView(it) }
        currentFooterView = WeakReference(view)
        return view
    }

    internal fun setContentState(content: TaiwaState) {
        if (currentContent == content) {
            return
        }
        currentContent = content
        dialogWrapper.setBackListenerEnabled(findBackAction() != null)
        val contentItems = (content.header?.items.orEmpty() + content.body?.items.orEmpty())
        val footerItems = content.footer?.items.orEmpty()
        val transitionContent = dialogWrapper.prepareTransition()
        dialogWrapper.beginViewTransition(transitionContent)
        dialogWrapper.setFooterVisible(footerItems.isNotEmpty())
        getContentView().setItems(contentItems, transitionContent)
        getFooterView().setItems(footerItems, transitionContent)
    }

    fun setContent(block: TaiwaScope.() -> Unit) {
        setContentState(buildTaiwa(block))
    }

    fun setEventListener(listener: (TaiwaEvent) -> Unit) {
        eventListener = listener
    }

    fun addDelegate(delegate: Envoy<DiffItem>) {
        getContentView().addDelegate(delegate)
        getFooterView().addDelegate(delegate)
    }

    fun show() {
        dialogWrapper.show()
    }

    fun close() {
        dialogWrapper.close()
    }

    private fun findBackAction(): TaiwaAction? {
        return currentContent?.backAction
    }

    private fun handleAction(action: TaiwaAction) {
        when (action) {
            TaiwaAction.Close -> dialogWrapper.close()
            TaiwaAction.Root -> eventListener?.invoke(TaiwaEvent.Anchor(TaiwaAnchor.Root))
            is TaiwaAction.Anchor -> eventListener?.invoke(TaiwaEvent.Anchor(action.anchor))
        }
    }
}