package ru.radiationx.anilibria.ui.fragments.releases

import android.os.Bundle
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import java.util.*

/* Created by radiationx on 05.11.17. */

class ReleasesFragment : BaseFragment(), ReleasesView, ReleasesAdapter.ItemListener {
    override val layoutRes: Int = R.layout.fragment_releases
    private var adapter: ReleasesAdapter = ReleasesAdapter()

    companion object {
        const val SUKA = "su4ara_rv"
    }

    @InjectPresenter
    lateinit var presenter: ReleasesPresenter

    @ProvidePresenter
    fun provideReleasesPresenter(): ReleasesPresenter {
        return ReleasesPresenter(App.injections.releasesRepository,
                (parentFragment as RouterProvider).router)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@ReleasesFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }

        adapter.setListener(this)
        toolbar.apply {
            title = getString(R.string.fragment_title_releases)
            menu.add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener({
                        presenter.openSearch()
                        //Toast.makeText(context, "Временно не поддерживается", Toast.LENGTH_SHORT).show()
                        false
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showReleases(releases: ArrayList<ReleaseItem>) {
        Log.e("SUKA", "showReleases")
        adapter.bindItems(releases)
    }

    override fun insertMore(releases: ArrayList<ReleaseItem>) {
        Log.e("SUKA", "insertMore")
        adapter.insertMore(releases)
    }

    override fun onLoadMore() {
        Log.e("SUKA", "onLoadMore")
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    var lsastpost = -1
    override fun onItemClick(item: ReleaseItem, position: Int) {
        lsastpost = position
        presenter.onItemClick(item)
    }

    lateinit var lastView: View

    override fun onItemClick(position: Int, view: View) {
        Log.e("SUKA", "ONITEMCLIC " + view)
        this.lastView = view;
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        return presenter.onItemLongClick(item)
    }


    fun getSharedView(): View {
        val viewItem = recyclerView.getLayoutManager().findViewByPosition(lsastpost)
        val icon = viewItem.findViewById<ImageView>(R.id.item_image)
        //lastView.transitionName = ReleaseFragment.TRANSACTION
        return lastView
    }
}
