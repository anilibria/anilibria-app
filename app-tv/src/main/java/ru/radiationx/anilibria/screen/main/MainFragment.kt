package ru.radiationx.anilibria.screen.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import androidx.leanback.widget.*
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.BaseRowsFragment
import ru.radiationx.anilibria.ui.presenter.ReleaseCardPresenter
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import java.time.DayOfWeek
import java.util.*
import javax.inject.Inject

class MainFragment : BaseRowsFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    @Inject
    lateinit var mockData: MockData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rowsAdapter.clear()
        createRow1()
        createRow2()
        adapter = rowsAdapter
        mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private fun createRow1() {
        val presenterSelector = ReleaseCardPresenter()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.feed.map { it.toCard() })
        }
        val headerItem = HeaderItem("Самое актуальное")

        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    private fun createRow2() {
        val presenterSelector = ReleaseCardPresenter()
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val items = mockData.schedule.first { it.day == day }.items
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, items.map { it.releaseItem.toCard() })
        }
        val headerItem = HeaderItem("Ожидается сегодня")

        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    @SuppressLint("DefaultLocale")
    private fun ReleaseItem.toCard() = LibriaCard(
        id,
        title.orEmpty(),
        "${seasons.firstOrNull()} год, ${genres.firstOrNull()
            ?.capitalize()}, Обновлен ${Date(torrentUpdate * 1000L).relativeDate().decapitalize()}",
        poster.orEmpty(),
        LibriaCard.Type.RELEASE
    )

    private fun YoutubeItem.toCard() = LibriaCard(
        id,
        title.orEmpty(),
        "Вышел ${Date(timestamp * 1000L).relativeDate().decapitalize()}",
        image.orEmpty(),
        LibriaCard.Type.YOUTUBE
    )

    private fun FeedItem.toCard(): LibriaCard = when {
        release != null -> release!!.toCard()
        youtube != null -> youtube!!.toCard()
        else -> throw RuntimeException("WataFuq")
    }

    private fun Date.relativeDate() = DateUtils.getRelativeDateTimeString(
        requireContext(),
        time,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.DAY_IN_MILLIS * 2,
        DateUtils.FORMAT_SHOW_TIME
    ).toString()

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            if (item is LibriaCard && rowViewHolder is CustomListRowViewHolder) {
                rowViewHolder.setDescription(item.title, item.description)
            }
        }
    }

}