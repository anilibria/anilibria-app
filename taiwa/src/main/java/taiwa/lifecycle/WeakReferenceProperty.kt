package taiwa.lifecycle

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


internal fun <T : Any> weakReference(value: T? = null): ReadWriteProperty<Any, T?> {
    return object : ReadWriteProperty<Any, T?> {

        private var weakRef = WeakReference(value)

        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            return weakRef.get()
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            weakRef = WeakReference(value)
        }
    }
}