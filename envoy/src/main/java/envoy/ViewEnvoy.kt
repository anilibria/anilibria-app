package envoy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class ViewEnvoy<T>(
    itemClass: Class<T>,
    @LayoutRes private val layoutRes: Int,
) : Envoy<T>(itemClass) {

    protected fun createView(parent: ViewGroup): View {
        val layoutInflater = LayoutInflater.from(parent.context)
        return layoutInflater.inflate(layoutRes, parent, false)
    }
}