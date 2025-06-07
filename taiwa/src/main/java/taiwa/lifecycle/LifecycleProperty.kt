package taiwa.lifecycle

import androidx.annotation.CallSuper
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class LifecycleProperty<in R : Any, out T : Destroyable>(
    private val creator: (R) -> T,
) : ReadOnlyProperty<R, T> {

    private var _value: T? = null

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return _value ?: creator(thisRef).also { _value = it }
    }

    @CallSuper
    open fun clear() {
        _value?.onDestroy()
        _value = null
    }
}


