package ru.radiationx.anilibria.screen.schedule

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.VerticalGridPresenter
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.common.fragment.scoped.ScopedBrowseFragment
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import javax.inject.Inject

class ScheduleFragment : ScopedBrowseFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val viewModel by viewModel<ScheduleViewModel>()

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)

        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        title = "Расписание"

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            backgroundManager.applyCard(item)
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaCard -> {
                        rowViewHolder.setDescription(item.title, item.description)
                    }
                    is LinkCard -> {
                        rowViewHolder.setDescription(item.title, "")
                    }
                    is LoadingCard -> {
                        rowViewHolder.setDescription(item.title, item.description)
                    }
                    else -> {
                        rowViewHolder.setDescription("", "")
                    }
                }
            }
        }

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is LibriaCard) {
                viewModel.onCardClick(item)
            }
        }

        adapter = rowsAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeTo(viewModel.scheduleRows) {
            val cardsPresenter = CardPresenterSelector()
            val rows = it.mapIndexed { index, day ->
                val cardsAdapter = ArrayObjectAdapter(cardsPresenter)
                cardsAdapter.setItems(day.second, CardDiffCallback)
                ListRow(index.toLong(), HeaderItem(day.first), cardsAdapter)
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
        }
    }
}