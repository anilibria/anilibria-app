package ru.radiationx.anilibria.ui.adapters.other

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class DividerShadowItemDelegate : AppAdapterDelegate<DividerShadowListItem, ListItem, DividerShadowItemDelegate.ViewHolder>(
        R.layout.item_other_divider_shadow,
        { it is DividerShadowListItem },
        { ViewHolder(it) }
)  {
     class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}
