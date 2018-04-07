package ru.radiationx.anilibria.ui.fragments.release.list

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.presentation.release.list.ReleasesPresenter
import ru.radiationx.anilibria.presentation.release.list.ReleasesView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.Utils

/* Created by radiationx on 05.11.17. */

class ReleasesFragment : BaseFragment(), SharedProvider, ReleasesView, ReleasesAdapter.ItemListener {

    private val adapter: ReleasesAdapter = ReleasesAdapter(this, PlaceholderListItem(
            R.drawable.ic_releases,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_base
    ))

    @InjectPresenter
    lateinit var presenter: ReleasesPresenter

    @ProvidePresenter
    fun provideReleasesPresenter(): ReleasesPresenter = ReleasesPresenter(
            App.injections.releaseRepository,
            App.injections.vitalRepository,
            (parentFragment as RouterProvider).getRouter(),
            App.injections.errorHandler
    )

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("S_DEF_LOG", "TEST onViewCreated " + this)
        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@ReleasesFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_releases)
            /*menu.add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener({
                        presenter.openSearch()
                        //Toast.makeText(context, "Временно не поддерживается", Toast.LENGTH_SHORT).show()
                        false
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)*/
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showVitalBottom(vital: VitalItem) {
        vitalBottom.visibility = View.VISIBLE
    }

    override fun showVitalItems(vital: List<VitalItem>) {
        adapter.setVitals(vital)
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
        this.sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        context?.let {
            val titles = arrayOf("Копировать ссылку", "Поделиться")
            AlertDialog.Builder(it)
                    .setItems(titles, { dialog, which ->
                        when (which) {
                            0 -> {
                                Utils.copyToClipBoard(item.link.orEmpty())
                                Toast.makeText(it, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                            }
                            1 -> Utils.shareText(item.link.orEmpty())
                        }
                    })
                    .show()
        }
        return false
    }

    /*override fun onItemLongClick(item: ReleaseItem): Boolean {
        return presenter.onItemLongClick(item)
    }*/
}
