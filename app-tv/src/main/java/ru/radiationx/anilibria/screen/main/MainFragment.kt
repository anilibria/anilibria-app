package ru.radiationx.anilibria.screen.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import androidx.leanback.widget.*
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.GridScreen
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class MainFragment : ScopedRowsFragment() {

    private val instantLoading = true
    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("kekeke", "$this oncreate $savedInstanceState")

        adapter = rowsAdapter
        onItemViewSelectedListener = ItemViewSelectedListener()

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {
                if (item is LibriaCard) {
                    router.navigateTo(DetailsScreen())
                } else {
                    router.navigateTo(GridScreen())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (rowsAdapter.size() == 0) {
            createRow1()
            createRow2()
            createRow3()
            createRow4()
        }
    }

    override fun onResume() {
        super.onResume()

        mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
    }

    private fun getLoadDelay() = if (instantLoading) {
        0L
    } else {
        (100..3000).random().toLong()
    }

    private fun createRow1() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            add(LoadingCard())
        }
        val headerItem = HeaderItem("Самое актуальное")
        rowsAdapter.add(ListRow(headerItem, adapter))

        Handler().postDelayed({
            adapter.apply {
                clear()
                //add(LoadingCard("Erororr", "Oh got wtf is goin on"))
                addAll(0, mockData.feed.shuffled().map { it.toCard(requireContext()) } + LinkCard("Смотреть всю ленту"))

            }
        }, getLoadDelay())
    }

    private fun createRow2() {
        val presenterSelector = CardPresenterSelector()
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val items = mockData.schedule.first { it.day == day }.items
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            add(LoadingCard())
        }
        val headerItem = HeaderItem("Ожидается сегодня")

        rowsAdapter.add(ListRow(headerItem, adapter))

        Handler().postDelayed({
            adapter.apply {
                clear()
                addAll(0, items.shuffled().map { it.releaseItem.toCard(requireContext()) })
                add(LinkCard("Открыть расписание"))
            }
        }, getLoadDelay())
    }

    private fun createRow3() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            add(LoadingCard())
        }
        val headerItem = HeaderItem("Обновления в избранном")

        rowsAdapter.add(ListRow(headerItem, adapter))

        Handler().postDelayed({
            adapter.apply {
                clear()
                addAll(0, mockData.releases.shuffled().map { it.toCard(requireContext()) })
                add(LinkCard("Открыть избранное"))
            }
        }, getLoadDelay())
    }

    private fun createRow4() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            add(LoadingCard())
        }
        val headerItem = HeaderItem("Обновления на YouTube")

        rowsAdapter.add(ListRow(headerItem, adapter))

        Handler().postDelayed({
            adapter.apply {
                clear()
                addAll(0, mockData.youtube.shuffled().map { it.toCard(requireContext()) })
                add(LinkCard("Открыть ролики YouTube"))
            }
        }, getLoadDelay())
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
                        rowViewHolder.setDescription(item.errorTitle, item.errorDescription)
                    }
                    else -> {
                        rowViewHolder.setDescription("", "")
                    }
                }
            }

        }
    }

}

fun ReleaseItem.toCard(context: Context) = LibriaCard(
    id,
    title.orEmpty(),
    "${seasons.firstOrNull()} год • ${genres.firstOrNull()
        ?.capitalize()} • Серии: ${series} • Обновлен ${Date(torrentUpdate * 1000L).relativeDate(context).decapitalize()}",
    poster.orEmpty(),
    LibriaCard.Type.RELEASE
)

fun YoutubeItem.toCard(context: Context) = LibriaCard(
    id,
    title.orEmpty(),
    "Вышел ${Date(timestamp * 1000L).relativeDate(context).decapitalize()}",
    image.orEmpty(),
    LibriaCard.Type.YOUTUBE
)

fun FeedItem.toCard(context: Context): LibriaCard = when {
    release != null -> release!!.toCard(context)
    youtube != null -> youtube!!.toCard(context)
    else -> throw RuntimeException("WataFuq")
}

fun Date.relativeDate(context: Context) = DateUtils.getRelativeDateTimeString(
    context,
    time,
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.DAY_IN_MILLIS * 2,
    DateUtils.FORMAT_SHOW_TIME
).toString()