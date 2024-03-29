package ru.radiationx.anilibria.ui.activities.updatechecker.adapter

import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateDataState
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.domain.updater.UpdateData

class UpdateContentAdapter(
    private val actionClickListener: (UpdateData.UpdateLink) -> Unit,
    private val cancelClickListener: (UpdateData.UpdateLink) -> Unit,
) : ListItemAdapter() {

    init {
        addDelegate(UpdateLinkDelegate(actionClickListener, cancelClickListener))
        addDelegate(UpdateInfoDelegate())
    }

    fun bindState(content: UpdateDataState) {
        val newItems = buildList {
            content.links.forEach {
                add(UpdateLinkListItem(it))
            }
            content.info.forEach { info ->
                if (info.items.isNotEmpty()) {
                    add(UpdateInfoListItem(info.title, info.items))
                }
            }
        }
        items = newItems
    }
}