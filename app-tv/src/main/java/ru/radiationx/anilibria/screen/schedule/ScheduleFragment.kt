package ru.radiationx.anilibria.screen.schedule

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import ru.radiationx.anilibria.common.CardDiffCallback
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.RowDiffCallback
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowViewHolder
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class ScheduleFragment : BrowseSupportFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val backgroundManager by lazy { GradientBackgroundManager(requireActivity()) }

    private val viewModel by viewModel<ScheduleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        title = "Расписание"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        setOnItemViewSelectedListener { _, item, rowViewHolder, _ ->
            backgroundManager.applyCard(item)
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaCard -> rowViewHolder.setDescription(item.title, item.description)
                    is LinkCard -> rowViewHolder.setDescription(item.title, "")
                    is LoadingCard -> rowViewHolder.setDescription(item.title, item.description)
                    else -> rowViewHolder.setDescription("", "")
                }
            }
        }

        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is LibriaCard) {
                viewModel.onCardClick(item)
            }
        }

        adapter = rowsAdapter

        subscribeTo(viewModel.scheduleRows) {
            val rows = it.mapIndexed { index, day ->
                val cardsPresenter = CardPresenterSelector(null)
                val cardsAdapter = ArrayObjectAdapter(cardsPresenter)
                cardsAdapter.setItems(day.second, CardDiffCallback)
                ListRow(index.toLong(), HeaderItem(day.first), cardsAdapter)
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
        }
    }
}
