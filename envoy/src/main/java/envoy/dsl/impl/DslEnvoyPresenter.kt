package envoy.dsl.impl

import android.view.View
import envoy.EnvoyPresenter

class DslEnvoyPresenter<T, V>(
    view: View,
    private val scope: DslEnvoyPresenterScope<T, V>
) : EnvoyPresenter<T>(view) {

    override fun bind(item: T) {
        scope.bindScopes.forEach { it.invoke(scope, item) }
    }

    override fun attach() {
        scope.attachScopes.forEach { it.invoke(scope) }
    }

    override fun detach() {
        scope.detachScopes.forEach { it.invoke(scope) }
    }
}