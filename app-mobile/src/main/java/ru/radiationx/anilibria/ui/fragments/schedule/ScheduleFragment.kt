package ru.radiationx.anilibria.ui.fragments.schedule

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list_refresh.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.schedule.SchedulePresenter
import ru.radiationx.anilibria.presentation.schedule.ScheduleView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.injectDependencies

class ScheduleFragment : BaseFragment(), ScheduleView, SharedProvider {

    companion object {
        private const val ARG_DAY = "arg day"
        fun newInstance(day: Int = -1) = ScheduleFragment().putExtra {
            putInt(ARG_DAY, day)
        }
    }

    private val scheduleAdapter = ScheduleAdapter(
        scheduleClickListener = { item, view, position ->
            this.sharedViewLocal = view
            presenter.onItemClick(item, position)
        },
        scrollListener = { position ->
            presenter.onHorizontalScroll(position)
        }
    )

    @InjectPresenter
    lateinit var presenter: SchedulePresenter

    @ProvidePresenter
    fun providePresenter(): SchedulePresenter =
        getDependency(SchedulePresenter::class.java, screenScope)

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
        arguments?.apply {
            presenter.argDay = getInt(ARG_DAY, presenter.argDay)
        }
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
            disableItemChangeAnimation()
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

    override fun showState(state: ScheduleScreenState) {
        refreshLayout.isRefreshing = state.refreshing
        scheduleAdapter.bindState(state)
    }

    override fun scrollToDay(day: ScheduleDayState) {
        val position = scheduleAdapter.getPositionByDay(day)
        (recyclerView.layoutManager as? LinearLayoutManager)?.also {
            it.scrollToPositionWithOffset(position, 0)
        }
    }
}