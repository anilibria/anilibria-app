package taiwa.internal

import taiwa.TaiwaAnchor
import taiwa.dsl.TaiwaContentScope
import taiwa.dsl.TaiwaRootContentScope
import taiwa.internal.dsl.TaiwaContentScopeImpl
import taiwa.internal.dsl.TaiwaRootContentScopeImpl
import taiwa.internal.models.TaiwaContentState
import taiwa.internal.models.TaiwaRootContentState

internal fun buildTaiwa(block: TaiwaContentScope.() -> Unit): TaiwaContentState {
    val scope = TaiwaContentScopeImpl(TaiwaAnchor.Root)
    block.invoke(scope)
    val state = scope.build()
    return state
}

internal fun buildRootTaiwa(block: TaiwaRootContentScope.() -> Unit): TaiwaRootContentState {
    val contentScope = TaiwaContentScopeImpl(TaiwaAnchor.Root)
    val scope = TaiwaRootContentScopeImpl(contentScope)
    block.invoke(scope)
    val state = scope.build()
    return state
}