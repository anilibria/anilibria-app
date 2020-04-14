package ru.radiationx.anilibria.screen.search

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedSearchFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.widget.manager.ExternalProgressManager
import ru.radiationx.anilibria.ui.widget.manager.ExternalTextManager
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import javax.inject.Inject

class SearchFragment : ScopedSearchFragment(), SearchSupportFragment.SearchResultProvider {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val progressManager by lazy { ExternalProgressManager() }
    private val emptyTextManager by lazy { ExternalTextManager() }

    private val viewModel by viewModel<SearchViewModel>()

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)

        backgroundManager.clearGradient()

        setSearchResultProvider(this)
        setOnItemViewSelectedListener(ItemViewSelectedListener())
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            when (item) {
                is LibriaCard -> viewModel.onCardClick(item)
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

        createCardsRowBy()
    }

    private fun createCardsRowBy() {
        val cardsPresenter = CardPresenterSelector()
        val cardsAdapter = ArrayObjectAdapter(cardsPresenter)
        val row = ListRow(1L, HeaderItem("Результат поиска"), cardsAdapter)
        subscribeTo(viewModel.resultData) {
            if (it.isEmpty()) {
                emptyTextManager.show()
                backgroundManager.clearGradient()
                rowsAdapter.setItems(emptyList<Row>(), RowDiffCallback)
            } else {
                emptyTextManager.hide()
                rowsAdapter.setItems(listOf(row), RowDiffCallback)
            }

            cardsAdapter.setItems(it, CardDiffCallback)
        }
        subscribeTo(viewModel.progressState) {
            if (it) {
                progressManager.show()
                emptyTextManager.hide()
            } else {
                progressManager.hide()
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.onQueryChange(query.orEmpty())
        return true
    }

    override fun onQueryTextChange(newQuery: String?): Boolean {
        viewModel.onQueryChange(newQuery.orEmpty())
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