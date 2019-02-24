package ru.radiationx.anilibria.ui.adapters.other

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

class DividerShadowItemDelegate : AppAdapterDelegate<DividerShadowListItem, ListItem, DividerShadowItemDelegate.ViewHolder>(
        R.layout.item_other_divider_shadow,
        { it is DividerShadowListItem },
        { ViewHolder(it) }
)  {
     class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
