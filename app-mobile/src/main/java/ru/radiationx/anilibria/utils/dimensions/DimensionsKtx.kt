package ru.radiationx.anilibria.utils.dimensions

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.anilibria.utils.lifecycle.attachedCoroutineScope
import ru.radiationx.quill.get

fun ViewHolder.applyDimensions(block: (Dimensions) -> Unit) {
    val provider = get<DimensionsProvider>()
    provider.observe()
        .onEach { block.invoke(it) }
        .launchIn(attachedCoroutineScope)
}