package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_remind.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseRemindListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseRemindDelegate(
    private val itemListener: () -> Unit
) : AppAdapterDelegate<ReleaseRemindListItem, ListItem, ReleaseRemindDelegate.ViewHolder>(
    R.layout.item_release_remind,
    { it is ReleaseRemindListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseRemindListItem, holder: ViewHolder) = holder.bind(item.text)

    class ViewHolder(
        override val containerView: View,
        private val itemListener: () -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            remindClose.setOnClickListener {
                itemListener.invoke()
            }
        }

        fun bind(item: String) {
            item_title.text = item
        }
    }

}