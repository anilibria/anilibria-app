package ru.radiationx.anilibria.ui.fragments.releases

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import java.util.*

/* Created by radiationx on 05.11.17. */

class ReleasesFragment : BaseFragment(), ReleasesView, ReleasesAdapter.ItemListener {
    override val layoutRes: Int = R.layout.fragment_releases
    private var adapter: ReleasesAdapter = ReleasesAdapter()

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

    override fun onItemClick(item: ReleaseItem) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        return presenter.onItemLongClick(item)
    }
}
