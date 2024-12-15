package ru.radiationx.anilibria.ui.adapters.other

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemOtherMenuBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.utils.dimensions.applyDimensions
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

        fun bind(state: OtherMenuItemState) {
            applyDimensions {

            }
            binding.otherMenuTitle.text = state.title
            binding.otherMenuIcon.setCompatDrawable(state.iconRes)
            binding.root.setOnClickListener { clickListener(state) }
        }
    }
}
