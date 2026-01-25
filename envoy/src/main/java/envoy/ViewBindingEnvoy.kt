package envoy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class ViewBindingEnvoy<T, VB : ViewBinding>(
    itemClass: Class<T>,
    bindingClass: Class<VB>,
) : Envoy<T>(itemClass) {

    private val inflateMethod by lazy {
        bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
    }

    @Suppress("UNCHECKED_CAST")
    protected fun createBinding(parent: ViewGroup): VB {
        val layoutInflater = LayoutInflater.from(parent.context)
        return inflateMethod(null, layoutInflater, parent, false) as VB
    }
}