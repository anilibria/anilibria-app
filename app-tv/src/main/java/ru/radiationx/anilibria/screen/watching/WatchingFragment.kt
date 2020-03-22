package ru.radiationx.anilibria.screen.watching

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.shared_app.di.viewModelFromParent
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class WatchingFragment : ScopedRowsFragment() {

    companion object {
        private const val HISTORY_ROW_ID = 1L
        private const val CONTINUE_ROW_ID = 2L
        private const val RECOMMENDS_ROW_ID = 3L
        private const val FAVORITES_ROW_ID = 4L
    }

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val historyViewModel by viewModelFromParent<HistoryViewModel>()
    private val recommendsViewModel by viewModelFromParent<RecommendsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(historyViewModel)
        lifecycle.addObserver(recommendsViewModel)
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            val viewMode: BaseCardsViewModel? = when ((row as ListRow).id) {
                HISTORY_ROW_ID -> historyViewModel
                RECOMMENDS_ROW_ID -> recommendsViewModel
                else -> null
            }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (rowsAdapter.size() == 0) {
            createCardsRowBy(HISTORY_ROW_ID, rowsAdapter, historyViewModel)
            createCardsRowBy(RECOMMENDS_ROW_ID, rowsAdapter, recommendsViewModel)
        }
    }
}