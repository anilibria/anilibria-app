package ru.radiationx.anilibria.ui.adapters.global

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_load_more.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.gone
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class LoadMoreDelegate(
        private val listener: Listener
) : AppAdapterDelegate<LoadMoreListItem, ListItem, LoadMoreDelegate.ViewHolder>(
        R.layout.item_load_more,
        { it is LoadMoreListItem },
        { ViewHolder(it, listener) }
) {

    override fun bindData(item: LoadMoreListItem, holder: ViewHolder) = holder.bind()

    class ViewHolder(
            override val containerView: View,
            private val listener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            itemLoadMoreBtn.gone()
            itemLoadMoreContainer.visible()
        }

        fun bind() {
            Log.d("S_DEF_LOG", "BIND LOAD_MORE")
            listener.onLoadMore()
        }
    }

    interface Listener {
        fun onLoadMore()
    }
}