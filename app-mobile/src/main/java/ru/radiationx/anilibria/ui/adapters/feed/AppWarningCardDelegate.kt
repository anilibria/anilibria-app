package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_app_warning_card.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.AppWarningCardListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.feed.FeedAppWarning

/**
 * Created by radiationx on 13.01.18.
 */
class AppWarningCardDelegate(
    private val clickListener: (FeedAppWarning) -> Unit,
    private val closeClickListener: (FeedAppWarning) -> Unit
) : AppAdapterDelegate<AppWarningCardListItem, ListItem, AppWarningCardDelegate.ViewHolder>(
    R.layout.item_app_warning_card,
    { it is AppWarningCardListItem },
    { ViewHolder(it, clickListener, closeClickListener) }
) {

    override fun bindData(item: AppWarningCardListItem, holder: ViewHolder) =
        holder.bind(item.warning)

    class ViewHolder(
        override val containerView: View,
        private val clickListener: (FeedAppWarning) -> Unit,
        private val closeClickListener: (FeedAppWarning) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(warning: FeedAppWarning) {
            containerView.setOnClickListener { clickListener.invoke(warning) }
            btClose.setOnClickListener { closeClickListener.invoke(warning) }
            tvContent.text = warning.title
        }
    }
}