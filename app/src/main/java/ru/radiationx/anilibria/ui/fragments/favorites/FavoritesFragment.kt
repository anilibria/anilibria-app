package ru.radiationx.anilibria.ui.fragments.favorites

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.favorites.FavoritesPresenter
import ru.radiationx.anilibria.presentation.favorites.FavoritesView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter

/**
 * Created by radiationx on 13.01.18.
 */
class FavoritesFragment : BaseFragment(), SharedProvider, FavoritesView, ReleasesAdapter.ItemListener {
    private val adapter: ReleasesAdapter = ReleasesAdapter(this)

    @InjectPresenter
    lateinit var presenter: FavoritesPresenter

    @ProvidePresenter
    fun provideFavoritesPresenter(): FavoritesPresenter {
        return FavoritesPresenter(App.injections.releaseRepository,
                (parentFragment as RouterProvider).router)
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("SUKA", "TEST onViewCreated " + this)
        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@FavoritesFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_favorites)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showReleases(releases: List<ReleaseItem>) {
        Log.e("SUKA", "fav show releases "+releases.size)
        adapter.bindItems(releases)
    }

    override fun insertMore(releases: List<ReleaseItem>) {
        adapter.insertMore(releases)
    }

    override fun onLoadMore() {
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view;
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        return presenter.onItemLongClick(item)
    }
}