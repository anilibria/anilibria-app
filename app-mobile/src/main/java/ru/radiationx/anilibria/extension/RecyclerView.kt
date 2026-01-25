package ru.radiationx.anilibria.extension

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun RecyclerView.disableItemChangeAnimation() {
    (itemAnimator as? DefaultItemAnimator?)?.supportsChangeAnimations = false
}

suspend fun <T> AsyncListDifferDelegationAdapter<T>.setAndAwaitItems(items: List<T>) {
    suspendCancellableCoroutine { continuation ->
        setItems(items) {
            continuation.resume(Unit)
        }
    }
}