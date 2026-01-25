package ru.radiationx.anilibria.ui.adapters.other

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemOtherMenuBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared.ktx.android.setCompatDrawable

class MenuItemDelegate(
    private val clickListener: (OtherMenuItemState) -> Unit
) : AppAdapterDelegate<MenuListItem, ListItem, MenuItemDelegate.ViewHolder>(
    R.layout.item_other_menu,
    { it is MenuListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: MenuListItem, holder: ViewHolder) = holder.bind(item.menuItem)

    class ViewHolder(
        itemView: View,
        val clickListener: (OtherMenuItemState) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemOtherMenuBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(state: OtherMenuItemState) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.otherMenuTitle.text = state.title
            binding.otherMenuIcon.setCompatDrawable(state.iconRes)
            binding.root.setOnClickListener { clickListener(state) }
        }
    }
}
