package ru.radiationx.anilibria.ui.adapters.global

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.CommentRouteListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 21.01.18.
 */
class CommentRouteDelegate(
    private val clickListener: () -> Unit
) : AppAdapterDelegate<CommentRouteListItem, ListItem, CommentRouteDelegate.ViewHolder>(
    R.layout.item_comment_route,
    { it is CommentRouteListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: CommentRouteListItem, holder: ViewHolder) {
        holder.bind()
    }

    class ViewHolder(view: View, clickListener: () -> Unit) : RecyclerView.ViewHolder(view) {

        private val dimensionsApplier by dimensionsApplier()

        init {
            view.setOnClickListener { clickListener.invoke() }
        }

        fun bind() {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
        }
    }
}