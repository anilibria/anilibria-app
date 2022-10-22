package ru.radiationx.anilibria.ui.adapters.global

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_load_more.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible

/**
 * Created by radiationx on 13.01.18.
 */
class LoadMoreDelegate(
    private val listener: (() -> Unit)?
) : AppAdapterDelegate<LoadMoreListItem, ListItem, LoadMoreDelegate.ViewHolder>(
    R.layout.item_load_more,
    { it is LoadMoreListItem },
    { ViewHolder(it, listener) }
) {

    override fun bindData(item: LoadMoreListItem, holder: ViewHolder) = holder.bind(item.needNotify)

    class ViewHolder(
        override val containerView: View,
        private val listener: (() -> Unit)?
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            itemLoadMoreBtn.gone()
            itemLoadMoreContainer.visible()
        }

        fun bind(needNotify: Boolean) {
            if (needNotify) {
                listener?.invoke()
            }
        }
    }
}