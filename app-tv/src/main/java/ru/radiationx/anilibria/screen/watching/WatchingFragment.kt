package ru.radiationx.anilibria.screen.watching

import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.quillParentViewModel

class WatchingFragment : RowsSupportFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val backgroundManager by inject<GradientBackgroundManager>()

    private val watchingViewModel by quillParentViewModel<WatchingViewModel>()

    private val historyViewModel by quillParentViewModel<WatchingHistoryViewModel>()
    private val continueViewModel by quillParentViewModel<WatchingContinueViewModel>()
    private val favoritesViewModel by quillParentViewModel<WatchingFavoritesViewModel>()
    private val recommendsViewModel by quillParentViewModel<WatchingRecommendsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(watchingViewModel)
        lifecycle.addObserver(historyViewModel)
        lifecycle.addObserver(continueViewModel)
        lifecycle.addObserver(favoritesViewModel)
        lifecycle.addObserver(recommendsViewModel)
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            val viewMode: BaseCardsViewModel? = getViewModel((row as ListRow).id)
            when (item) {
                is LinkCard -> viewMode?.onLinkCardClick()
                is LoadingCard -> viewMode?.onLoadingCardClick()
                is LibriaCard -> viewMode?.onLibriaCardClick(item)
            }
        }

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {
                backgroundManager.applyCard(item)
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
        adapter = rowsAdapter
    }

    private fun getViewModel(rowId: Long): BaseCardsViewModel? = when (rowId) {
        WatchingViewModel.HISTORY_ROW_ID -> historyViewModel
        WatchingViewModel.CONTINUE_ROW_ID -> continueViewModel
        WatchingViewModel.FAVORITES_ROW_ID -> favoritesViewModel
        WatchingViewModel.RECOMMENDS_ROW_ID -> recommendsViewModel
        else -> null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rowMap = mutableMapOf<Long, ListRow>()
        subscribeTo(watchingViewModel.rowListData) { rowList ->
            val rows = rowList.map { rowId ->
                val row =
                    rowMap[rowId] ?: createCardsRowBy(rowId, rowsAdapter, getViewModel(rowId)!!)
                rowMap[rowId] = row
                row
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
        }
    }
}