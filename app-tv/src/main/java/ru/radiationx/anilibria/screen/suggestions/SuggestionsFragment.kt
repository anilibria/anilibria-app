package ru.radiationx.anilibria.screen.suggestions

import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.ViewModel
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardDiffCallback
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.RowDiffCallback
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.ui.widget.manager.ExternalProgressManager
import ru.radiationx.anilibria.ui.widget.manager.ExternalTextManager
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.quillModule
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class SuggestionsFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val progressManager by lazy { ExternalProgressManager() }
    private val emptyTextManager by lazy { ExternalTextManager() }

    // РАНЬШЕ: private val backgroundManager by inject<GradientBackgroundManager>()
    private val backgroundManager by lazy { GradientBackgroundManager(requireActivity()) }

    private val rowsViewModel by viewModel<SuggestionsRowsViewModel>()
    private val resultViewModel by viewModel<SuggestionsResultViewModel>()
    private val recommendsViewModel by viewModel<SuggestionsRecommendsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Если раньше стояла installModules(ActivityModule(this)) — убрали
        installModules(quillModule {
            single<SuggestionsController>()
        })
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(rowsViewModel)
        viewLifecycleOwner.lifecycle.addObserver(resultViewModel)
        viewLifecycleOwner.lifecycle.addObserver(recommendsViewModel)

        backgroundManager.clearGradient()

        setSearchResultProvider(this)
        setOnItemViewSelectedListener(ItemViewSelectedListener())
        setOnItemViewClickedListener { _, item, rowViewHolder, row ->
            when (val vm = getViewModel((row as ListRow).id)) {
                is BaseCardsViewModel -> {
                    when (item) {
                        is LinkCard -> vm.onLinkCardClick()
                        is LoadingCard -> vm.onLoadingCardClick()
                        is LibriaCard -> vm.onLibriaCardClick(item)
                    }
                }

                is SuggestionsResultViewModel -> {
                    if (item is LibriaCard) vm.onCardClick(item)
                }
            }
        }

        progressManager.rootView = view as ViewGroup
        progressManager.initialDelay = 0L

        emptyTextManager.rootView = view
        emptyTextManager.initialDelay = 0L
        emptyTextManager.text = "Ничего не найдено"

        subscribeTo(rowsViewModel.emptyResultState) { isEmpty ->
            if (isEmpty) {
                emptyTextManager.show()
                backgroundManager.clearGradient()
            } else {
                emptyTextManager.hide()
            }
        }

        val rowMap = mutableMapOf<Long, Row>()
        subscribeTo(rowsViewModel.rowListData) { rowList ->
            val rows = rowList.map { rowId ->
                val row = rowMap[rowId] ?: createRowBy(rowId)
                rowMap[rowId] = row
                row
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
        }
    }

    override fun onPause() {
        avoidSpeechRecognitinCrash()
        super.onPause()
    }

    private fun getViewModel(rowId: Long): Any? = when (rowId) {
        SuggestionsRowsViewModel.RESULT_ROW_ID -> resultViewModel
        SuggestionsRowsViewModel.RECOMMENDS_ROW_ID -> recommendsViewModel
        else -> null
    }

    private fun createRowBy(rowId: Long): Row {
        return when (rowId) {
            SuggestionsRowsViewModel.RESULT_ROW_ID -> {
                // Результат
                val cardsPresenter = CardPresenterSelector(null)
                val cardsAdapter = ArrayObjectAdapter(cardsPresenter)
                val row = ListRow(rowId, HeaderItem("Результат поиска"), cardsAdapter)

                subscribeTo(resultViewModel.resultData) { list ->
                    cardsAdapter.setItems(list, CardDiffCallback)
                }
                subscribeTo(resultViewModel.progressState) { loading ->
                    if (loading) progressManager.show() else progressManager.hide()
                }
                row
            }

            SuggestionsRowsViewModel.RECOMMENDS_ROW_ID -> {
                // Рекомендации
                createCardsRowBy(rowId, rowsAdapter, recommendsViewModel)
            }

            else -> ListRow(rowId, HeaderItem("???"), ArrayObjectAdapter())
        }
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
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row,
        ) {
            if (rowViewHolder is CustomListRowViewHolder) {
                backgroundManager.applyCard(item)
                when (item) {
                    is LibriaCard -> rowViewHolder.setDescription(item.title, item.description)
                    is LinkCard -> rowViewHolder.setDescription(item.title, "")
                    is LoadingCard -> rowViewHolder.setDescription(item.title, item.description)
                    else -> rowViewHolder.setDescription("", "")
                }
            }
        }
    }

    private fun avoidSpeechRecognitinCrash() {
        try {
            val speechRecField =
                SearchSupportFragment::class.java.getDeclaredField("mSpeechRecognizer")
            val searchBarField = SearchSupportFragment::class.java.getDeclaredField("mSearchBar")
            speechRecField.isAccessible = true
            searchBarField.isAccessible = true

            val sr = speechRecField.get(this) ?: return
            val sb = searchBarField.get(this)
            val setSpeechRecMethod = sb::class.java.getDeclaredMethod(
                "setSpeechRecognizer",
                SpeechRecognizer::class.java
            )
            setSpeechRecMethod.isAccessible = true
            setSpeechRecMethod.invoke(sb, null)

            val destroyMethod = sr::class.java.getDeclaredMethod("destroy")
            destroyMethod.isAccessible = true
            destroyMethod.invoke(sr)

            speechRecField.set(this, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
