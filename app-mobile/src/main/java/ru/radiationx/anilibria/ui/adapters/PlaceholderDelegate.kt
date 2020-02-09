package ru.radiationx.anilibria.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_placeholder.*
import ru.radiationx.anilibria.R
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.setTintColorAttr
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class PlaceholderDelegate : AppAdapterDelegate<PlaceholderListItem, ListItem, PlaceholderDelegate.ViewHolder>(
        R.layout.item_placeholder,
        { it is PlaceholderListItem },
        { ViewHolder(it) }
) {

    override fun bindData(item: PlaceholderListItem, holder: ViewHolder) =
            holder.bind(item.icRes, item.titleRes, item.descRes)

    class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(icRes: Int, titleRes: Int, descRes: Int) {
            item_placeholder_icon.setCompatDrawable(icRes)
            item_placeholder_icon.setTintColorAttr(R.attr.colorOnSurface)
            item_placeholder_title.setText(titleRes)
            item_placeholder_desc.setText(descRes)
        }
    }
}
