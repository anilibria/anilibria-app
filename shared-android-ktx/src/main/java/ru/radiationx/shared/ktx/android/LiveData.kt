package ru.radiationx.shared.ktx.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

inline fun <T> Fragment.subscribeTo(liveData: LiveData<T>, crossinline action: (T) -> Unit) = liveData.observe(viewLifecycleOwner, Observer { action(it) })

inline fun <T> FragmentActivity.subscribeTo(liveData: LiveData<T>, crossinline action: (T) -> Unit) = liveData.observe(this, Observer { action(it) })

