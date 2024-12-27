package ru.radiationx.combineflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.radiationx.combineflow.api.CombineBuilderScope
import ru.radiationx.combineflow.internal.CombineBuilderScopeImpl

fun <T> buildCombineFlow(block: suspend CombineBuilderScope<T>.() -> Unit): Flow<T> = flow {
    val builderScope = CombineBuilderScopeImpl<T>()
    block.invoke(builderScope)
    val collector = builderScope.buildCollector()
    val properties = builderScope.buildProperties()
    val flows = properties.map { it.flow }
    emitAll(combine(flows) {
        it.forEachIndexed { index, value ->
            properties[index].setValue(value)
        }
        collector.block.invoke()
    })
}

