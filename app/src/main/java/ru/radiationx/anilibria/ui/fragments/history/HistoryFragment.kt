package ru.radiationx.anilibria.ui.fragments.history

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.history.HistoryPresenter
import ru.radiationx.anilibria.presentation.history.HistoryView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.ToolbarHelper

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryFragment : BaseFragment(), HistoryView, SharedProvider, ReleasesAdapter.ItemListener {

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    private val adapter = ReleasesAdapter(this)
    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun provideHistoryPresenter(): HistoryPresenter = HistoryPresenter(
            (parentFragment as RouterProvider).router,
            App.injections.historyRepository
    )

    override fun getLayoutResource(): Int = R.layout.fragment_history

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarHelper.fixInsets(toolbar)

        toolbar.apply {
            title = "История"
            setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@HistoryFragment.adapter
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }
    }

    override fun setRefreshing(refreshing: Boolean) {}

    override fun showReleases(releases: List<ReleaseItem>) {
        adapter.bindItems(releases)
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        presenter.onItemLongClick(item)
        return false
    }

    override fun onLoadMore() {}

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

}