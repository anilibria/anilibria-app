package ru.radiationx.anilibria.screen.trash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.View
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.GridScreen
import ru.radiationx.anilibria.screen.main.relativeDate
import ru.radiationx.anilibria.screen.main.toCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.presenter.ReleaseDetailsPresenter
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class TestDetailFragment : ScopedRowsFragment() {

    private val instantLoading = true
    private val rowsPresenter by lazy { ClassPresenterSelector() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }
    private val details by lazy { mockData.releases.random().toDetail(requireContext()) }

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("kekeke", "$this oncreate $savedInstanceState")

        adapter = rowsAdapter
        rowsPresenter.addClassPresenter(ListRow::class.java, CustomListRowPresenter())
        rowsPresenter.addClassPresenter(LibriaDetailsRow::class.java, ReleaseDetailsPresenter())

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.e("kekeke", "select $item, $row")
            if (row is ListRow) {
                backgroundManager.applyCard(item)
            } else {
                backgroundManager.applyImage(details.image)
            }
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaCard -> {
                        rowViewHolder.setDescription(item.title, item.description)
                    }
                    is LinkCard -> {
                        rowViewHolder.setDescription(item.title, "")
                    }
                    is LoadingCard -> {
                        rowViewHolder.setDescription(item.errorTitle, item.errorDescription)
                    }
                    else -> {
                        rowViewHolder.setDescription("", "")
                    }
                }
            }
        }

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {

                router.navigateTo(GridScreen())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (rowsAdapter.size() == 0) {
            createDetailRow()
            createRow1()
        }
    }


    private fun createDetailRow() {

        backgroundManager.applyImage(details.image)
        rowsAdapter.add(LibriaDetailsRow(details))
    }

    private fun createRow1() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.feed.shuffled().map { it.toCard(requireContext()) } + LinkCard("Смотреть всю ленту"))
        }
        val headerItem = HeaderItem("Самое актуальное")
        rowsAdapter.add(ListRow(headerItem, adapter))
    }

}

fun ReleaseItem.toDetail(context: Context) = LibriaDetails(
    id,
    title.orEmpty(),
    titleEng.orEmpty(),
    listOf(
        "${seasons.firstOrNull()} года",
        genres.firstOrNull()?.capitalize()?.trim(),
        types.firstOrNull()?.trim(),
        "Серии: ${series?.trim()}"
    ).joinToString(" • "),
    Html.fromHtml(description.orEmpty()).toString().trim(),
    announce.orEmpty(),
    poster.orEmpty()
)