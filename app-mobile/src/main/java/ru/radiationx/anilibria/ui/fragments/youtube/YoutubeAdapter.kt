package ru.radiationx.anilibria.ui.fragments.youtube

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class YoutubeAdapter(
    private val loadRetryListener: () -> Unit,
    private val listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        addDelegate(YoutubeDelegate(listener))
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

    fun bindState(state: YoutubeScreenState) {
        val newItems = mutableListOf<ListItem>()

        val loadingState = state.data

        if (loadingState.data?.isEmpty() == true && !loadingState.emptyLoading) {
            newItems.add(placeHolder)
        }

        loadingState.data?.let { data ->
            newItems.addAll(data.map { YoutubeListItem(it) })
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

    interface ItemListener : LoadMoreDelegate.Listener, YoutubeDelegate.Listener

}
