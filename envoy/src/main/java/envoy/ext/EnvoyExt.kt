package envoy.ext

import android.view.View
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import envoy.Envoy
import envoy.dsl.EnvoyPresenterScope
import envoy.dsl.impl.DslViewBindingEnvoy
import envoy.dsl.impl.DslViewEnvoy

inline fun <reified T : Any, reified VB : ViewBinding> viewBindingEnvoy(
    noinline block: EnvoyPresenterScope<T, VB>.() -> Unit
): Envoy<T> {
    return DslViewBindingEnvoy(T::class.java, VB::class.java, block)
}

inline fun <reified T : Any> viewEnvoy(
    @LayoutRes layoutRes: Int,
    noinline block: EnvoyPresenterScope<T, View>.() -> Unit
): Envoy<T> {
    return DslViewEnvoy(T::class.java, layoutRes, block)
}