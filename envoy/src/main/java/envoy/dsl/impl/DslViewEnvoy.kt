package envoy.dsl.impl

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import envoy.EnvoyPresenter
import envoy.ViewEnvoy
import envoy.dsl.EnvoyPresenterScope

class DslViewEnvoy<T>(
    itemClass: Class<T>,
    @LayoutRes layoutRes: Int,
    private val block: EnvoyPresenterScope<T, View>.() -> Unit
) : ViewEnvoy<T>(itemClass, layoutRes) {

    override fun onCreate(parent: ViewGroup): EnvoyPresenter<T> {
        val view = createView(parent)
        val scope = DslEnvoyPresenterScope<T, View>(view)
        block.invoke(scope)
        return DslEnvoyPresenter(view, scope)
    }
}