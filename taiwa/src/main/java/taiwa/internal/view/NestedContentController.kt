package taiwa.internal.view

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import taiwa.TaiwaAnchor
import taiwa.internal.models.TaiwaState
import taiwa.internal.models.TaiwaNestingState

internal class NestedContentController {


    private val anchorFlow = MutableStateFlow<TaiwaAnchor>(TaiwaAnchor.Root)

    private val rootStateFlow = MutableStateFlow<TaiwaNestingState?>(null)

    val currentStateFlow = combine(anchorFlow, rootStateFlow.filterNotNull()) { anchor, rootState ->
        transformState(anchor, rootState)
    }.distinctUntilChanged()

    fun apply(state: TaiwaNestingState) {
        rootStateFlow.value = state
    }

    fun toAnchor(anchor: TaiwaAnchor) {
        val content = rootStateFlow.value?.let { findContentByAnchor(anchor, it) }
        anchorFlow.value = anchor.takeIf { content != null } ?: TaiwaAnchor.Root
    }

    private fun transformState(
        anchor: TaiwaAnchor,
        root: TaiwaNestingState,
    ): TaiwaState {
        val contentByAnchor = findContentByAnchor(anchor, root)
        return contentByAnchor ?: root.content
    }

    private fun findContentByAnchor(
        anchor: TaiwaAnchor,
        root: TaiwaNestingState,
    ): TaiwaState? {
        return when (anchor) {
            TaiwaAnchor.Root -> root.content
            is TaiwaAnchor.Id -> root.nested[anchor]
        }
    }
}