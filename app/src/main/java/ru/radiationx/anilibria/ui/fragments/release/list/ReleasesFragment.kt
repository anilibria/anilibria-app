package ru.radiationx.anilibria.ui.fragments.release.list

import android.graphics.Color
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
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.release.list.ReleasesPresenter
import ru.radiationx.anilibria.presentation.release.list.ReleasesView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider

/* Created by radiationx on 05.11.17. */

class ReleasesFragment : BaseFragment(), SharedProvider, ReleasesView, ReleasesAdapter.ItemListener {

    private val adapter: ReleasesAdapter = ReleasesAdapter()

    @InjectPresenter
    lateinit var presenter: ReleasesPresenter

    @ProvidePresenter
    fun provideReleasesPresenter(): ReleasesPresenter {
        return ReleasesPresenter(App.injections.releaseRepository,
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
        Log.e("SUKA", "TEST onViewCreated "+this)
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

    override fun onDetach() {
        super.onDetach()
        Log.e("SUKA", "RELEASE LIST onDetach")

    }

    override fun onPause() {
        super.onPause()
        Log.e("SUKA", "RELEASE LIST onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("SUKA", "RELEASE LIST onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("SUKA", "RELEASE LIST onDestroy")
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showReleases(releases: List<ReleaseItem>) {
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
