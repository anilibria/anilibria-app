package ru.radiationx.anilibria.ui.fragments.other

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.adapters.ProfileListItem
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.other.MenuItemDelegate
import ru.radiationx.anilibria.ui.adapters.other.ProfileItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.anilibria.ui.fragments.auth.otp.OtpAcceptDialogFragment
import ru.radiationx.quill.viewModel


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseDimensionsFragment(R.layout.fragment_list) {

    private val adapter = OtherAdapter()

    private val binding by viewBinding<FragmentListBinding>()

    private val viewModel by viewModel<OtherViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@OtherFragment.adapter
            disableItemChangeAnimation()
        }

        viewModel.refresh()

        viewModel.state.onEach { state ->
            adapter.bindItems(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.otpEvent.onEach {
            OtpAcceptDialogFragment().show(childFragmentManager, "otp_f")
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    inner class OtherAdapter : ListItemAdapter() {

        private val profileClickListener = { _: ProfileItemState -> viewModel.onProfileClick() }

        private val logoutClickListener = { viewModel.signOut() }

        private val menuClickListener = { item: OtherMenuItemState -> viewModel.onMenuClick(item) }

        init {
            delegatesManager.apply {
                addDelegate(ProfileItemDelegate(profileClickListener, logoutClickListener))
                addDelegate(DividerShadowItemDelegate())
                addDelegate(MenuItemDelegate(menuClickListener))
            }
        }

        fun bindItems(state: ProfileScreenState) {
            items = mutableListOf<ListItem>().apply {
                state.profile?.also { profile ->
                    add(ProfileListItem("profile", profile))
                    addAll(state.profileMenuItems.map { MenuListItem(it) })
                    add(DividerShadowListItem("profile"))
                }

                val lastItem = state.menuItems.lastOrNull()
                state.menuItems.forEach { menuItems ->
                    addAll(menuItems.map { MenuListItem(it) })
                    if (menuItems.isNotEmpty() && lastItem != menuItems) {
                        add(DividerShadowListItem("divider_${menuItems.lastOrNull()?.id ?: 0}"))
                    }
                }
            }
        }
    }
}
