package ru.radiationx.anilibria.utils.view

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

suspend fun View.awaitAttached() {
    suspendCancellableCoroutine { continuation ->
        doOnAttach { continuation.resume(Unit) }
    }

}

suspend fun View.awaitLayout() {
    suspendCancellableCoroutine { continuation ->
        doOnLayout { continuation.resume(Unit) }
    }
}

suspend fun View.awaitContainsInParent() {
    val viewRect = Rect()
    val containerRect = Rect()
    while (coroutineContext.isActive) {
        val parentViewGroup = (parent as? ViewGroup?)
        if (parentViewGroup != null) {
            getLocalVisibleRect(viewRect)
            parentViewGroup.getLocalVisibleRect(containerRect)
            val containsInRecycler = containerRect.contains(viewRect)
            if (containsInRecycler) {
                break
            }
        }
        delay((1000 / 60))
    }
}