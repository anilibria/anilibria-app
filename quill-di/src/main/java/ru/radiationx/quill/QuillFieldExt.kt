@file:Suppress("UnusedReceiverParameter", "UnusedReceiverParameter", "UnusedReceiverParameter")

package ru.radiationx.quill

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass


inline fun <reified T : Any> Fragment.inject(
    qualifier: KClass<out Annotation>? = null,
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    get(qualifier)
}

inline fun <reified T : Any> Fragment.get(
    qualifier: KClass<out Annotation>? = null,
): T {
    return getScope().get(T::class, qualifier)
}

inline fun <reified T : Any> FragmentActivity.inject(
    qualifier: KClass<out Annotation>? = null,
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    get(qualifier)
}

inline fun <reified T : Any> FragmentActivity.get(
    qualifier: KClass<out Annotation>? = null,
): T {
    return getScope().get(T::class, qualifier)
}

inline fun <reified T : Any> Context.inject(
    qualifier: KClass<out Annotation>? = null,
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    get(qualifier)
}

inline fun <reified T : Any> Context.get(
    qualifier: KClass<out Annotation>? = null,
): T {
    return Quill.getRootScope().get(T::class, qualifier)
}