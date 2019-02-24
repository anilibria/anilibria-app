package ru.radiationx.anilibria.ui.adapters

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_placeholder.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class PlaceholderDelegate : AppAdapterDelegate<PlaceholderListItem, ListItem, PlaceholderDelegate.ViewHolder>(
        R.layout.item_placeholder,
        { it is PlaceholderListItem },
        { ViewHolder(it) }
) {

    override fun bindData(item: PlaceholderListItem, holder: ViewHolder) =
            holder.bind(item.icRes, item.titleRes, item.descRes)

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(icRes: Int, titleRes: Int, descRes: Int) {
            view.run {
                item_placeholder_icon.setImageDrawable(ContextCompat.getDrawable(context, icRes))
                item_placeholder_icon.drawable?.setColorFilter(
                        context.getColorFromAttr(R.attr.base_icon),
                        PorterDuff.Mode.SRC_ATOP
                )
                item_placeholder_title.setText(titleRes)
                item_placeholder_desc.setText(descRes)
            }
        }
    }
}
