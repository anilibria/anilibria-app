package ru.radiationx.combineflow.internal

import kotlinx.coroutines.flow.Flow
import ru.radiationx.combineflow.api.CombineProperty
import kotlin.reflect.KProperty

internal class CombinePropertyImpl<T>(
    val flow: Flow<T>
) : CombineProperty<T> {

    private object Uninitialized

    private var value: Any? = Uninitialized

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        require(value !== Uninitialized) {
            "api.CombineProperty '${property.name}' has no value, probably the value is taken from outside the 'collect' method"
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    fun setValue(value: Any?) {
        this.value = value
    }
}