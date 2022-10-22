package ru.radiationx.anilibria.screen.suggestions

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModel
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedSearchFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.anilibria.screen.details.DetailsViewModel
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.widget.manager.ExternalProgressManager
import ru.radiationx.anilibria.ui.widget.manager.ExternalTextManager
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import toothpick.ktp.binding.module
import javax.inject.Inject

class SuggestionsFragment : ScopedSearchFragment(), SearchSupportFragment.SearchResultProvider {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val progressManager by lazy { ExternalProgressManager() }
    private val emptyTextManager by lazy { ExternalTextManager() }

    private val rowsViewModel by viewModel<SuggestionsRowsViewModel>()
    private val resultViewModel by viewModel<SuggestionsResultViewModel>()
    private val recommendsViewModel by viewModel<SuggestionsRecommendsViewModel>()

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        dependencyInjector.installModules(module {
            bind(SuggestionsController::class.java).singleton()
        })
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(rowsViewModel)
        lifecycle.addObserver(resultViewModel)
        lifecycle.addObserver(recommendsViewModel)

        backgroundManager.clearGradient()

        setSearchResultProvider(this)
        setOnItemViewSelectedListener(ItemViewSelectedListener())
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->

            val viewModel = getViewModel((row as ListRow).id)
            when (viewModel) {
                is BaseCardsViewModel -> when (item) {
                    is LinkCard -> viewModel.onLinkCardClick()
                    is LoadingCard -> viewModel.onLoadingCardClick()
                    is LibriaCard -> viewModel.onLibriaCardClick(item)
                }
                is SuggestionsResultViewModel -> when (item) {
                    is LibriaCard -> resultViewModel.onCardClick(item)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressManager.rootView = view as ViewGroup
        progressManager.initialDelay = 0L

        emptyTextManager.rootView = view as ViewGroup
        emptyTextManager.initialDelay = 0L
        emptyTextManager.text = "Ничего не найдено"


        subscribeTo(rowsViewModel.emptyResultState) {
            if (it) {
                emptyTextManager.show()
                backgroundManager.clearGradient()
            } else {
                emptyTextManager.hide()
            }
        }

        val rowMap = mutableMapOf<Long, Row>()
        subscribeTo(rowsViewModel.rowListData) { rowList ->
            val rows = rowList.map { rowId ->
                val row = rowMap[rowId] ?: createRowBy(rowId, rowsAdapter, getViewModel(rowId)!!)
                rowMap[rowId] = row
                row
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
        }
    }

    private fun getViewModel(rowId: Long): ViewModel? = when (rowId) {
        SuggestionsRowsViewModel.RESULT_ROW_ID -> resultViewModel
        SuggestionsRowsViewModel.RECOMMENDS_ROW_ID -> recommendsViewModel
        else -> null
    }

    private fun createRowBy(rowId: Long, rowsAdapter: ArrayObjectAdapter, viewModel: ViewModel): Row = when (rowId) {
        DetailsViewModel.RELEASE_ROW_ID -> createHeaderRowBy(rowId, rowsAdapter, viewModel as SuggestionsResultViewModel)
        else -> createCardsRowBy(rowId, rowsAdapter, viewModel as BaseCardsViewModel)
    }

    private fun createHeaderRowBy(rowId: Long, rowsAdapter: ArrayObjectAdapter, viewModel: SuggestionsResultViewModel): Row {
        val cardsPresenter = CardPresenterSelector()
        val cardsAdapter = ArrayObjectAdapter(cardsPresenter)
        val row = ListRow(rowId, HeaderItem("Результат поиска"), cardsAdapter)
        subscribeTo(viewModel.resultData) {
            cardsAdapter.setItems(it, CardDiffCallback)
        }
        subscribeTo(viewModel.progressState) {
            if (it) {
                progressManager.show()
            } else {
                progressManager.hide()
            }
        }
        return row
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        resultViewModel.onQueryChange(query.orEmpty())
        return true
    }

    override fun onQueryTextChange(newQuery: String?): Boolean {
        resultViewModel.onQueryChange(newQuery.orEmpty())
        return true
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
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
    }
}