package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.adapters.PlaceholderDelegate
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.AnchorListItem
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.shared_app.controllers.loaderpage.needShowPlaceholder

/* Created by radiationx on 31.10.17. */

class YoutubeAdapter(
    loadMoreListener: () -> Unit,
    loadRetryListener: () -> Unit,
    clickListener: (YoutubeItemState) -> Unit,
    longClickListener: (YoutubeItemState) -> Unit,
    private val emptyPlaceHolder: PlaceholderListItem,
    private val errorPlaceHolder: PlaceholderListItem,
) : ListItemAdapter() {

    init {
        addDelegate(YoutubeDelegate(clickListener, longClickListener))
        addDelegate(LoadMoreDelegate(loadMoreListener))
        addDelegate(LoadErrorDelegate(loadRetryListener))
        addDelegate(PlaceholderDelegate())
    }

    fun bindState(state: YoutubeScreenState) {
        val newItems = mutableListOf<ListItem>()
        newItems.add(AnchorListItem())

        val loadingState = state.data

        getPlaceholder(state)?.also {
            newItems.add(it)
        }

        loadingState.data?.let { data ->
            newItems.addAll(data.map { YoutubeListItem(it) })
        }

        if (loadingState.hasMoreData) {
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

}
