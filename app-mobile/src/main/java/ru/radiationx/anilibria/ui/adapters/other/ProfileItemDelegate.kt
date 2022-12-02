package ru.radiationx.anilibria.ui.adapters.other

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemOtherProfileBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ProfileListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.other.ProfileItemState
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.quill.Quill
import ru.radiationx.shared_app.imageloader.showImageUrl

class ProfileItemDelegate(
    private val clickListener: (ProfileItemState) -> Unit,
    private val logoutClickListener: () -> Unit
) : AppAdapterDelegate<ProfileListItem, ListItem, ProfileItemDelegate.ViewHolder>(
    R.layout.item_other_profile,
    { it is ProfileListItem },
    { ViewHolder(it, clickListener, logoutClickListener) }
) {

    override fun bindData(item: ProfileListItem, holder: ViewHolder) = holder.bind(item.state)

    class ViewHolder(
        itemView: View,
        private val clickListener: (ProfileItemState) -> Unit,
        private val logoutClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemOtherProfileBinding>()

        private val dimensionsProvider = Quill.getRootScope().get(DimensionsProvider::class)

        fun bind(state: ProfileItemState) {
            dimensionsProvider.get().also {
                binding.root.setPadding(
                    binding.root.paddingLeft,
                    it.statusBar,
                    binding.root.paddingRight,
                    binding.root.paddingBottom
                )
            }
            binding.profileNick.text = state.title
            binding.profileDesc.text = state.subtitle
            binding.profileLogout.isVisible = state.hasAuth
            binding.profileDesc.isVisible = state.subtitle != null
            binding.profileAvatar.showImageUrl(state.avatar)

            binding.root.setOnClickListener { clickListener(state) }
            binding.profileLogout.setOnClickListener { logoutClickListener() }
        }
    }
}
