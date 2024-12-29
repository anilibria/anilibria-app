package envoy.dsl.impl

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import envoy.EnvoyPresenter
import envoy.ViewBindingEnvoy
import envoy.dsl.EnvoyPresenterScope

class DslViewBindingEnvoy<T, VB : ViewBinding>(
    itemClass: Class<T>,
    bindingClass: Class<VB>,
    private val block: EnvoyPresenterScope<T, VB>.() -> Unit
) : ViewBindingEnvoy<T, VB>(itemClass, bindingClass) {

    override fun onCreate(parent: ViewGroup): EnvoyPresenter<T> {
        val binding = createBinding(parent)
        val scope = DslEnvoyPresenterScope<T, VB>(binding)
        block.invoke(scope)
        return DslEnvoyPresenter(binding.root, scope)
    }
}