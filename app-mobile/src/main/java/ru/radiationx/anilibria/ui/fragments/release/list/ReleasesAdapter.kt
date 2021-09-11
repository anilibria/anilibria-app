package ru.radiationx.anilibria.ui.fragments.release.list

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class ReleasesAdapter(
    private val loadRetryListener: () -> Unit,
    private val listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        addDelegate(ReleaseItemDelegate(listener))
        addDelegate(LoadMoreDelegate(null))
        addDelegate(LoadErrorDelegate(loadRetryListener))
        addDelegate(PlaceholderDelegate())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any?>
    ) {
        super.onBindViewHolder(holder, position, payloads)

        val threshold = (items.lastIndex - position)
        if (threshold <= 3) {
            Handler().post {
                listener.onLoadMore()
            }
        }
    }

    fun bindState(loadingState: DataLoadingState<List<ReleaseItemState>>) {
        val newItems = mutableListOf<ListItem>()

        if (loadingState.data?.isEmpty() == true && !loadingState.emptyLoading) {
            newItems.add(placeHolder)
        }

        loadingState.data?.let { data ->
            newItems.addAll(data.map { ReleaseListItem(it) })
        }

        if (loadingState.hasMorePages) {
            if (loadingState.error != null) {
                newItems.add(LoadErrorListItem("bottom"))
            } else if (loadingState.moreLoading) {
                newItems.add(LoadMoreListItem("bottom"))
            }
        }

        items = newItems
    }

    interface ItemListener : LoadMoreDelegate.Listener, ReleaseItemDelegate.Listener
}
