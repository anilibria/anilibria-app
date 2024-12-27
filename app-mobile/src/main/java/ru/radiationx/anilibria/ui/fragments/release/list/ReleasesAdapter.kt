package ru.radiationx.anilibria.ui.fragments.release.list

import android.view.View
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.adapters.PlaceholderDelegate
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.ShadowDirection
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState
import ru.radiationx.shared_app.controllers.loaderpage.needShowPlaceholder

/* Created by radiationx on 31.10.17. */

class ReleasesAdapter(
    loadMoreListener: () -> Unit,
    loadRetryListener: () -> Unit,
    importListener: (() -> Unit)? = null,
    exportListener: (() -> Unit)? = null,
    clickListener: (ReleaseItemState, View) -> Unit,
    longClickListener: (ReleaseItemState) -> Unit,
    private val emptyPlaceHolder: PlaceholderListItem,
    private val errorPlaceHolder: PlaceholderListItem,
) : ListItemAdapter() {

    companion object {
        private const val TAG_IMPORT = "import"
        private const val TAG_EXPORT = "expor"
    }

    init {
        addDelegate(ReleaseItemDelegate(clickListener, longClickListener))
        addDelegate(LoadMoreDelegate(loadMoreListener))
        addDelegate(LoadErrorDelegate(loadRetryListener))
        addDelegate(PlaceholderDelegate())
        addDelegate(FeedSectionDelegate {
            when (it.tag) {
                TAG_IMPORT -> importListener?.invoke()
                TAG_EXPORT -> exportListener?.invoke()
            }
        })
        addDelegate(DividerShadowItemDelegate())
    }

    fun bindState(
        loadingState: PageLoaderState<List<ReleaseItemState>>,
        withExport: Boolean = false,
    ) {
        val newItems = mutableListOf<ListItem>()
        if (withExport) {
            newItems.add(FeedSectionListItem(TAG_IMPORT, "Импортировать историю", hasBg = true))
            newItems.add(FeedSectionListItem(TAG_EXPORT, "Экспортировать историю", hasBg = true))
        }

        getPlaceholder(loadingState)?.also {
            newItems.add(it)
        }

        loadingState.data?.let { data ->
            if (withExport) {
                val historyShadow = if (data.isEmpty()) {
                    ShadowDirection.Bottom
                } else {
                    ShadowDirection.Double
                }
                newItems.add(DividerShadowListItem(historyShadow, "history"))
            }
            newItems.addAll(data.map { ReleaseListItem(it) })
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


    private fun getPlaceholder(loadingState: PageLoaderState<List<ReleaseItemState>>): PlaceholderListItem? {
        val needPlaceholder = loadingState.needShowPlaceholder { it?.isNotEmpty() ?: false }
        return when {
            needPlaceholder && loadingState.error != null -> errorPlaceHolder
            needPlaceholder && loadingState.error == null -> emptyPlaceHolder
            else -> null
        }
    }
}
