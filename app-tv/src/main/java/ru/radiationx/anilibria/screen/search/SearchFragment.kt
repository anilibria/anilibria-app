package ru.radiationx.anilibria.screen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.widget.SearchTitleView
import ru.radiationx.anilibria.ui.widget.manager.ExternalTextManager
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class SearchFragment : BaseVerticalGridFragment() {

    private val cardsPresenter = CardPresenterSelector {
        cardsViewModel.onLinkCardBind()
    }
    private val cardsAdapter = ArrayObjectAdapter(cardsPresenter)

    private val emptyTextManager by lazy { ExternalTextManager() }

    private val backgroundManager by inject<GradientBackgroundManager>()

    private val cardsViewModel by viewModel<SearchViewModel>()
    private val formViewModel by viewModel<SearchFormViewModel>()

    override fun onInflateTitleView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.lb_search_titleview, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(cardsViewModel)
        viewLifecycleOwner.lifecycle.addObserver(formViewModel)

        title = "Каталог"
        gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = 6
        }

        setOnSearchClickedListener {
            cardsViewModel.onSearchClick()
        }

        backgroundManager.clearGradient()
        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            backgroundManager.applyCard(item)
            when (item) {
                is LibriaCard -> {
                    setDescription(item.title, item.description)
                }
                is LinkCard -> {
                    setDescription(item.title, "")
                }
                is LoadingCard -> {
                    setDescription(item.title, item.description)
                }
                else -> {
                    setDescription("", "")
                }
            }
        }

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            when (item) {
                is LinkCard -> {
                    cardsViewModel.onLinkCardClick()
                }
                is LoadingCard -> {
                    cardsViewModel.onLoadingCardClick()
                }
                is LibriaCard -> {
                    cardsViewModel.onLibriaCardClick(item)
                }
            }
        }

        prepareEntranceTransition()
        adapter = cardsAdapter

        emptyTextManager.rootView = view as ViewGroup
        emptyTextManager.initialDelay = 0L
        emptyTextManager.text = "По данным параметрам ничего не найдено"

        (titleView as? SearchTitleView?)?.apply {
            setYearClickListener(View.OnClickListener { formViewModel.onYearClick() })
            setSeasonClickListener(View.OnClickListener { formViewModel.onSeasonClick() })
            setGenreClickListener(View.OnClickListener { formViewModel.onGenreClick() })
            setSortClickListener(View.OnClickListener { formViewModel.onSortClick() })
            setOnlyCompletedClickListener(View.OnClickListener { formViewModel.onOnlyCompletedClick() })

            subscribeTo(formViewModel.yearData) { year = it }
            subscribeTo(formViewModel.seasonData) { season = it }
            subscribeTo(formViewModel.genreData) { genre = it }
            subscribeTo(formViewModel.sortData) { sort = it }
            subscribeTo(formViewModel.onlyCompletedData) { onlyCompleted = it }
        }

        progressBarManager.initialDelay = 0

        subscribeTo(cardsViewModel.progressState) {
            if (it) {
                progressBarManager.show()
            } else {
                progressBarManager.hide()
            }
        }

        subscribeTo(cardsViewModel.cardsData) {
            if (it.isEmpty()) {
                backgroundManager.clearGradient()
                setDescriptionVisible(false)
                emptyTextManager.show()
            } else {
                emptyTextManager.hide()
                setDescriptionVisible(true)
            }
            cardsAdapter.setItems(it, CardDiffCallback)
            startEntranceTransition()
        }
    }
}