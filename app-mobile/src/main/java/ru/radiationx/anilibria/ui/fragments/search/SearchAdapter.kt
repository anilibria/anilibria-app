package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseRemindDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter

/**
 * Created by radiationx on 04.03.18.
 */
class SearchAdapter(
    private val loadMoreListener: () -> Unit,
    private val loadRetryListener: () -> Unit,
    private val listener: ReleasesAdapter.ItemListener,
    private val remindCloseListener: () -> Unit,
    private val placeholder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        delegatesManager.run {
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
            addDelegate(ReleaseItemDelegate(listener))
            addDelegate(LoadMoreDelegate(loadMoreListener))
            addDelegate(LoadErrorDelegate(loadRetryListener))
            addDelegate(PlaceholderDelegate())
        }
    }

    fun bindState(state: SearchScreenState) {
        val newItems = mutableListOf<ListItem>()

        val loadingState = state.data

        if (loadingState.data?.isEmpty() == true && !loadingState.emptyLoading) {
            state.remindText?.also {
                newItems.add(ReleaseRemindListItem(it))
            }
            newItems.add(placeholder)
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
}