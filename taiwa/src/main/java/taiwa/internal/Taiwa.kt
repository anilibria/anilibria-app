package taiwa.internal

import taiwa.TaiwaAnchor
import taiwa.dsl.TaiwaScope
import taiwa.dsl.TaiwaNestingScope
import taiwa.internal.dsl.TaiwaScopeImpl
import taiwa.internal.dsl.TaiwaNestingScopeImpl
import taiwa.internal.models.TaiwaState
import taiwa.internal.models.TaiwaNestingState

internal fun buildTaiwa(block: TaiwaScope.() -> Unit): TaiwaState {
    val scope = TaiwaScopeImpl(TaiwaAnchor.Root)
    block.invoke(scope)
    val state = scope.build()
    return state
}

internal fun buildNestingTaiwa(block: TaiwaNestingScope.() -> Unit): TaiwaNestingState {
    val contentScope = TaiwaScopeImpl(TaiwaAnchor.Root)
    val scope = TaiwaNestingScopeImpl(contentScope)
    block.invoke(scope)
    val state = scope.build()
    return state
}