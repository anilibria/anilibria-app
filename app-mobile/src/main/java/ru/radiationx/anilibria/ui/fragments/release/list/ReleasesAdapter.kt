package ru.radiationx.anilibria.ui.fragments.release.list

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState
import ru.radiationx.anilibria.model.loading.needShowPlaceholder
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.adapters.PlaceholderDelegate
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class ReleasesAdapter(
    loadMoreListener: () -> Unit,
    loadRetryListener: () -> Unit,
    listener: ItemListener,
    private val emptyPlaceHolder: PlaceholderListItem,
    private val errorPlaceHolder: PlaceholderListItem,
) : ListItemAdapter() {

    init {
        addDelegate(ReleaseItemDelegate(listener))
        addDelegate(LoadMoreDelegate(loadMoreListener))
        addDelegate(LoadErrorDelegate(loadRetryListener))
        addDelegate(PlaceholderDelegate())
    }

    fun bindState(loadingState: DataLoadingState<List<ReleaseItemState>>) {
        val newItems = mutableListOf<ListItem>()

        getPlaceholder(loadingState)?.also {
            newItems.add(it)
        }

        loadingState.data?.let { data ->
            newItems.addAll(data.map { ReleaseListItem(it) })
        }

        if (loadingState.hasMorePages) {
            if (loadingState.error != null) {
                newItems.add(LoadErrorListItem("bottom"))
            } else {
                newItems.add(LoadMoreListItem("bottom", !loadingState.moreLoading))
            }
        }

        items = newItems
    }


    private fun getPlaceholder(loadingState: DataLoadingState<List<ReleaseItemState>>): PlaceholderListItem? {
        val needPlaceholder = loadingState.needShowPlaceholder { it?.isNotEmpty() ?: false }
        return when {
            needPlaceholder && loadingState.error != null -> errorPlaceHolder
            needPlaceholder && loadingState.error == null -> emptyPlaceHolder
            else -> null
        }
    }

    interface ItemListener : ReleaseItemDelegate.Listener
}
