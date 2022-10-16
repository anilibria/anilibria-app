package ru.radiationx.anilibria.ui.fragments.other

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.other.OtherPresenter
import ru.radiationx.anilibria.presentation.other.OtherView
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.adapters.ProfileListItem
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.other.MenuItemDelegate
import ru.radiationx.anilibria.ui.adapters.other.ProfileItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.auth.otp.OtpAcceptDialogFragment
import ru.radiationx.shared_app.di.injectDependencies


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseFragment(), OtherView {

    private val adapter = OtherAdapter()

    @InjectPresenter
    lateinit var presenter: OtherPresenter

    @ProvidePresenter
    fun provideOtherPresenter(): OtherPresenter =
        getDependency(OtherPresenter::class.java, screenScope)

    override fun getBaseLayout(): Int = R.layout.fragment_list

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@OtherFragment.adapter
            disableItemChangeAnimation()
        }
    }

    override fun showState(state: ProfileScreenState) {
        adapter.bindItems(state)
    }

    override fun showOtpCode() {
        OtpAcceptDialogFragment().show(childFragmentManager, "otp_f")
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    inner class OtherAdapter : ListItemAdapter() {

        private val profileClickListener = { _: ProfileItemState -> presenter.onProfileClick() }

        private val logoutClickListener = { presenter.signOut() }

        private val menuClickListener = { item: OtherMenuItemState -> presenter.onMenuClick(item) }

        init {
            delegatesManager.apply {
                addDelegate(ProfileItemDelegate(profileClickListener, logoutClickListener))
                addDelegate(DividerShadowItemDelegate())
                addDelegate(MenuItemDelegate(menuClickListener))
            }
        }

        fun bindItems(state: ProfileScreenState) {
            items = mutableListOf<ListItem>().apply {
                state.profile?.also {
                    add(ProfileListItem("profile", it))
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
