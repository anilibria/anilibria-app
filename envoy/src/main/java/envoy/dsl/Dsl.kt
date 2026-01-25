package envoy.dsl


@DslMarker
annotation class ViewScopeMarker

@ViewScopeMarker
interface EnvoyViewScope<V> {
    val view: V
}

interface EnvoyPresenterScope<T, V> : EnvoyViewScope<V> {

    fun bind(block: EnvoyViewScope<V>.(T) -> Unit)

    fun attach(block: EnvoyViewScope<V>.() -> Unit)

    fun detach(block: EnvoyViewScope<V>.() -> Unit)
}