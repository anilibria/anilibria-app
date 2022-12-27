package ru.radiationx.shared.ktx.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

inline fun <T> Fragment.subscribeTo(
    liveData: Flow<T>,
    crossinline action: (T) -> Unit
) {
    liveData.onEach {
        action.invoke(it)
    }.launchIn(viewLifecycleOwner.lifecycleScope)
}

inline fun <T> FragmentActivity.subscribeTo(
    liveData: Flow<T>,
    crossinline action: (T) -> Unit
) {
    liveData.onEach {
        action.invoke(it)
    }.launchIn(lifecycleScope)
}

