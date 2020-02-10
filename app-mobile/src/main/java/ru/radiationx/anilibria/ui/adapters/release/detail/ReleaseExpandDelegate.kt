package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_expand.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseExpandListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseExpandDelegate(
        private val clickListener: (ReleaseExpandListItem) -> Unit
) : AppAdapterDelegate<ReleaseExpandListItem, ListItem, ReleaseExpandDelegate.ViewHolder>(
        R.layout.item_release_expand,
        { it is ReleaseExpandListItem },
        { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 20

    override fun bindData(item: ReleaseExpandListItem, holder: ViewHolder) =
            holder.bind(item)

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (ReleaseExpandListItem) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: ReleaseExpandListItem

        init {
            item_expand_title.setOnClickListener { clickListener.invoke(currentItem) }
        }

        fun bind(item: ReleaseExpandListItem) {
            currentItem = item
            item_expand_title.text = item.title
        }

    }

}