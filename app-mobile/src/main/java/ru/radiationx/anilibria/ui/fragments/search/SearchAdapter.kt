package ru.radiationx.anilibria.ui.fragments.search

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
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
    private val loadRetryListener: () -> Unit,
    private val listener: ReleasesAdapter.ItemListener,
    private val remindCloseListener: ReleaseRemindDelegate.Listener,
    private val placeholder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        delegatesManager.run {
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
            addDelegate(ReleaseItemDelegate(listener))
            addDelegate(LoadMoreDelegate(null))
            addDelegate(LoadErrorDelegate(loadRetryListener))
            addDelegate(PlaceholderDelegate())
        }
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
            } else if (loadingState.moreLoading) {
                newItems.add(LoadMoreListItem("bottom"))
            }
        }

        items = newItems
    }
}