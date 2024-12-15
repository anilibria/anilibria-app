package ru.radiationx.anilibria.ui.adapters.other

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ShadowDirection
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class DividerShadowItemDelegate :
    AppAdapterDelegate<DividerShadowListItem, ListItem, DividerShadowItemDelegate.ViewHolder>(
        R.layout.item_other_divider_shadow,
        { it is DividerShadowListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: DividerShadowListItem, holder: ViewHolder) {
        holder.bind(item)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DividerShadowListItem) {
            val bgRes = when (item.direction) {
                ShadowDirection.Top -> R.drawable.pref_shadow_to_top
                ShadowDirection.Bottom -> R.drawable.pref_shadow_to_bottom
                ShadowDirection.Double -> R.drawable.pref_shadow_double
            }
            itemView.setBackgroundResource(bgRes)
        }
    }
}
