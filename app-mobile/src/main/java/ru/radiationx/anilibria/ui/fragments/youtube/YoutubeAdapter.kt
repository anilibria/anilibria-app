package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.model.loading.needShowPlaceholder
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class YoutubeAdapter(
    private val loadMoreListener: () -> Unit,
    private val loadRetryListener: () -> Unit,
    private val listener: ItemListener,
    private val emptyPlaceHolder: PlaceholderListItem,
    private val errorPlaceHolder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        addDelegate(YoutubeDelegate(listener))
        addDelegate(LoadMoreDelegate(loadMoreListener))
        addDelegate(LoadErrorDelegate(loadRetryListener))
        addDelegate(PlaceholderDelegate())
    }

    fun bindState(state: YoutubeScreenState) {
        val newItems = mutableListOf<ListItem>()

        val loadingState = state.data

        getPlaceholder(state)?.also {
            newItems.add(it)
        }

        loadingState.data?.let { data ->
            newItems.addAll(data.map { YoutubeListItem(it) })
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

    private fun getPlaceholder(state: YoutubeScreenState): PlaceholderListItem? {
        val loadingState = state.data
        val needPlaceholder = loadingState.needShowPlaceholder { it?.isNotEmpty() ?: false }
        return when {
            needPlaceholder && loadingState.error != null -> errorPlaceHolder
            needPlaceholder && loadingState.error == null -> emptyPlaceHolder
            else -> null
        }
    }

    interface ItemListener : YoutubeDelegate.Listener

}
