package ru.radiationx.anilibria.utils.lifecycle

import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import ru.radiationx.anilibria.R

val View.attachedCoroutineScope: CoroutineScope
    get() = getAttachedCoroutineScopeInternal(this)

val ViewHolder.attachedCoroutineScope: CoroutineScope
    get() = itemView.attachedCoroutineScope

private fun getAttachedCoroutineScopeInternal(view: View): CoroutineScope {
    val savedScope = view.getTag(R.id.attached_view_coroutine_scope) as? CoroutineScope?
    if (savedScope != null) {
        return savedScope
    }
    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    view.setTag(R.id.attached_view_coroutine_scope, scope)
    view.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
        }

        override fun onViewDetachedFromWindow(v: View) {
            view.setTag(R.id.attached_view_coroutine_scope, null)
            view.removeOnAttachStateChangeListener(this)
            scope.cancel()
        }
    })
    return scope
}