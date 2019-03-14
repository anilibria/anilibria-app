package ru.radiationx.anilibria.ui.fragments.schedule

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_list_refresh.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.presentation.schedule.SchedulePresenter
import ru.radiationx.anilibria.presentation.schedule.ScheduleView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.utils.ToolbarHelper

class ScheduleFragment : BaseFragment(), ScheduleView, SharedProvider {

    private val scheduleAdapter = ScheduleAdapter { item, view ->
        this.sharedViewLocal = view
        presenter.onItemClick(item.releaseItem)
    }

    @InjectPresenter
    lateinit var presenter: SchedulePresenter

    @ProvidePresenter
    fun providePresenter(): SchedulePresenter = getDependency(screenScope, SchedulePresenter::class.java)

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_list_refresh

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            ToolbarHelper.fixInsets(this)
            title = getString(R.string.fragment_title_schedule)
            setNavigationOnClickListener { presenter.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        ToolbarShadowController(recyclerView, appbarLayout) {
            updateToolbarShadow(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        scheduleAdapter.saveState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        scheduleAdapter.restoreState(savedInstanceState)
    }

    override fun onDestroyView() {
        scheduleAdapter.saveState(null)
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showSchedules(items: List<Pair<String, List<FeedScheduleItem>>>) {
        scheduleAdapter.bindItems(items)
    }

    override fun scrollToDay(item: Pair<String, List<FeedScheduleItem>>) {
        val position = scheduleAdapter.getPositionByDay(item)
        Log.e("ninini", "scrollToDay ${item.first} -> $position")
        (recyclerView.layoutManager as? LinearLayoutManager)?.also {
            it.scrollToPositionWithOffset(position, 0)
        }
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }
}