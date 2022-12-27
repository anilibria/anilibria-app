package ru.radiationx.quill

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass


fun <T : ViewModel> Fragment.getViewModel(
    clazz: KClass<T>,
    extraProvider: (() -> QuillExtra)? = null
): T {
    val factory = createViewModelFactory(clazz, getScope(), extraProvider)
    return ViewModelProvider(this, factory)[clazz.java]
}

fun <T : ViewModel> FragmentActivity.getViewModel(
    clazz: KClass<T>,
    extraProvider: (() -> QuillExtra)? = null
): T {
    val factory = createViewModelFactory(clazz, getScope(), extraProvider)
    return ViewModelProvider(this, factory)[clazz.java]
}

inline fun <reified T : ViewModel> Fragment.viewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    getViewModel(T::class, extraProvider)
}

inline fun <reified T : ViewModel> FragmentActivity.viewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    getViewModel(T::class, extraProvider)
}