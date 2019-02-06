package ru.radiationx.anilibria.ui.fragments.other

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_other.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.presentation.other.OtherPresenter
import ru.radiationx.anilibria.presentation.other.OtherView
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.adapters.ProfileListItem
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.other.MenuItemDelegate
import ru.radiationx.anilibria.ui.adapters.other.ProfileItemDelegate
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseFragment(), OtherView {

    private val adapter = OtherAdapter()

    @InjectPresenter
    lateinit var presenter: OtherPresenter

    @ProvidePresenter
    fun provideOtherPresenter(): OtherPresenter {
        return OtherPresenter(
                (parentFragment as RouterProvider).getRouter(),
                App.injections.authRepository,
                App.injections.errorHandler
        )
    }

    override fun getLayoutResource(): Int = View.NO_ID

    override fun getBaseLayout(): Int = R.layout.fragment_other

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@OtherFragment.adapter
        }
    }

    override fun showItems(profileItem: ProfileItem, menu: List<MutableList<OtherMenuItem>>) {
        adapter.apply {
            clear()
            addProfile(profileItem)
            menu.forEach { addMenu(it) }
            notifyDataSetChanged()
        }
    }

    override fun updateProfile() {
        adapter.notifyDataSetChanged()
    }

    override fun setRefreshing(refreshing: Boolean) {}

    override fun onBackPressed(): Boolean {
        return false
    }

    inner class OtherAdapter : ListDelegationAdapter<MutableList<ListItem>>() {

        private val profileClickListener = { item: ProfileItem ->
            if (item.authState == AuthState.AUTH) {
                (parentFragment as RouterProvider).getRouter().showSystemMessage("Просмотр профиля недоступен")
            } else {
                presenter.openAuth()
            }
        }

        private val logoutClickListener = { presenter.signOut() }

        private val menuClickListener = { item: OtherMenuItem -> presenter.onMenuClick(item) }

        init {
            items = mutableListOf()
            delegatesManager.run {
                addDelegate(ProfileItemDelegate(profileClickListener, logoutClickListener))
                addDelegate(DividerShadowItemDelegate())
                addDelegate(MenuItemDelegate(menuClickListener))
            }
        }

        fun clear() {
            items.clear()
        }

        fun addProfile(profileItem: ProfileItem) {
            items.add(ProfileListItem(profileItem))
            items.add(DividerShadowListItem())
        }

        fun addMenu(newItems: MutableList<OtherMenuItem>) {
            items.addAll(newItems.map { MenuListItem(it) })
            if (newItems.isNotEmpty()) {
                items.add(DividerShadowListItem())
            }
        }
    }
}
