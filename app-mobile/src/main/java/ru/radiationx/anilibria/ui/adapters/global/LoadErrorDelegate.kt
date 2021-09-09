package ru.radiationx.anilibria.ui.adapters.global

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_load_error.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class LoadErrorDelegate(
    private val retryListener: () -> Unit
) : AppAdapterDelegate<LoadErrorListItem, ListItem, LoadErrorDelegate.ViewHolder>(
    R.layout.item_load_error,
    { it is LoadErrorListItem },
    { ViewHolder(it, retryListener) }
) {

    override fun bindData(item: LoadErrorListItem, holder: ViewHolder) = holder.bind()

    class ViewHolder(
        override val containerView: View,
        private val listener: () -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind() {
            itemLoadErrorBtn.setOnClickListener {
                listener.invoke()
            }
        }
    }
}