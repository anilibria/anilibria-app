package ru.radiationx.anilibria.ui.fragments.schedule

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.putExtra

class ScheduleFragment : BaseToolbarFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider {

    companion object {
        private const val ARG_DAY = "arg day"
        fun newInstance(day: Int?) = ScheduleFragment().putExtra {
            putSerializable(ARG_DAY, day)
        }
    }

    private val scheduleAdapter = ScheduleAdapter(
        scheduleClickListener = { item, view, position ->
            this.sharedViewLocal = view
            viewModel.onItemClick(item, position)
        },
        scrollListener = { position ->
            viewModel.onHorizontalScroll(position)
        }
    )

    private val viewModel by viewModel<ScheduleViewModel> {
        ScheduleExtra(day = getExtra(ARG_DAY))
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentListRefreshBinding {
        return FragmentListRefreshBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseBinding.toolbar.apply {
            ToolbarHelper.fixInsets(this)
            title = getString(R.string.fragment_title_schedule)
            setNavigationOnClickListener { viewModel.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        binding.refreshLayout.setOnRefreshListener { viewModel.refresh() }

        binding.recyclerView.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        ToolbarShadowController(binding.recyclerView, baseBinding.appbarLayout) {
            updateToolbarShadow(it)
        }

        viewModel.state.onEach { state ->
            binding.refreshLayout.isRefreshing = state.refreshing
            scheduleAdapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.scrollEvent.onEach { day ->
            val position = scheduleAdapter.getPositionByDay(day)
            (binding.recyclerView.layoutManager as? LinearLayoutManager)?.also {
                it.scrollToPositionWithOffset(position, 0)
            }
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
}