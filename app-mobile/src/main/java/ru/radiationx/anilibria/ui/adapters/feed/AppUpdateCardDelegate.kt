package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_app_update_card.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.AppUpdateCardListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class AppUpdateCardDelegate(
    private val clickListener: () -> Unit,
    private val closeClickListener: () -> Unit
) : AppAdapterDelegate<AppUpdateCardListItem, ListItem, AppUpdateCardDelegate.ViewHolder>(
    R.layout.item_app_update_card,
    { it is AppUpdateCardListItem },
    { ViewHolder(it, clickListener, closeClickListener) }
) {

    override fun bindData(item: AppUpdateCardListItem, holder: ViewHolder) =
        holder.bind()

    class ViewHolder(
        override val containerView: View,
        private val clickListener: () -> Unit,
        private val closeClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            containerView.setOnClickListener { clickListener.invoke() }
            btClose.setOnClickListener { closeClickListener.invoke() }
        }

        fun bind() {}
    }
}