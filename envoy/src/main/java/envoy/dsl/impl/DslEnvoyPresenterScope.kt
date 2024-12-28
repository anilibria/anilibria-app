package envoy.dsl.impl

import envoy.dsl.EnvoyPresenterScope
import envoy.dsl.EnvoyViewScope

class DslEnvoyPresenterScope<T, V>(
    override val view: V
) : EnvoyPresenterScope<T, V> {

    val bindScopes = mutableListOf<EnvoyViewScope<V>.(T) -> Unit>()
    val attachScopes = mutableListOf<EnvoyViewScope<V>.() -> Unit>()
    val detachScopes = mutableListOf<EnvoyViewScope<V>.() -> Unit>()

    override fun bind(block: EnvoyViewScope<V>.(T) -> Unit) {
        bindScopes.add(block)
    }

    override fun attach(block: EnvoyViewScope<V>.() -> Unit) {
        attachScopes.add(block)
    }

    override fun detach(block: EnvoyViewScope<V>.() -> Unit) {
        detachScopes.add(block)
    }
}