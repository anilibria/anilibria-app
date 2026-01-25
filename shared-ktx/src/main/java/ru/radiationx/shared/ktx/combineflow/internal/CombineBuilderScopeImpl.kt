package ru.radiationx.combineflow.internal

import kotlinx.coroutines.flow.Flow
import ru.radiationx.combineflow.api.CombineBuilderScope
import ru.radiationx.combineflow.api.CombineProperty

internal class CombineBuilderScopeImpl<R> : CombineBuilderScope<R> {

    private val properties = mutableListOf<CombinePropertyImpl<*>>()

    private var collector: CombineCollector<R>? = null

    override fun <T> Flow<T>.register(): CombineProperty<T> {
        val property = CombinePropertyImpl(this)
        properties.add(property)
        return property
    }

    override fun collect(block: suspend () -> R) {
        require(collector == null) {
            "There can only be one collector"
        }
        collector = CombineCollector(block)
    }

    fun buildCollector(): CombineCollector<R> {
        return requireNotNull(collector) {
            "internal.CombineCollector not found"
        }
    }

    fun buildProperties(): List<CombinePropertyImpl<*>> {
        return properties.toList()
    }
}