package ru.radiationx.anilibria.screen.main

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import androidx.leanback.widget.*
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.anilibria.screen.GridScreen
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.shared_app.di.viewModelFromParent
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class MainFragment : ScopedRowsFragment() {

    companion object {
        private const val FEED_ROW_ID = 1L
        private const val SCHEDULE_ROW_ID = 2L
        private const val FAVORITE_ROW_ID = 3L
        private const val YOUTUBE_ROW_ID = 4L
    }

    private val instantLoading = true
    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val feedViewModel by viewModelFromParent<FeedViewModel>()
    private val scheduleViewModel by viewModelFromParent<ScheduleViewModel>()
    private val youtubeViewModel by viewModelFromParent<YouTubeViewModel>()

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(feedViewModel)
        lifecycle.addObserver(scheduleViewModel)
        lifecycle.addObserver(youtubeViewModel)

        Log.e("kekeke", "$this oncreate $savedInstanceState")

        adapter = rowsAdapter
        onItemViewSelectedListener = ItemViewSelectedListener()

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {
                Log.e("lalala", "onclick $item")
                val viewMode: BaseCardsViewModel? = when ((row as ListRow).id) {
                    FEED_ROW_ID -> feedViewModel
                    SCHEDULE_ROW_ID -> scheduleViewModel
                    YOUTUBE_ROW_ID -> youtubeViewModel
                    else -> null
                }
                when (item) {
                    is LinkCard -> {
                        viewMode?.onLinkCardClick()
                    }
                    is LoadingCard -> {
                        viewMode?.onLoadingCardClick()
                    }
                    is LibriaCard -> {
                        viewMode?.onLibriaCardClick(item)
                    }
                    else -> {
                        router.navigateTo(GridScreen())
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (rowsAdapter.size() == 0) {
            createCardsRowBy(FEED_ROW_ID, rowsAdapter, feedViewModel)
            createCardsRowBy(SCHEDULE_ROW_ID, rowsAdapter, scheduleViewModel)
            createCardsRowBy(YOUTUBE_ROW_ID, rowsAdapter, youtubeViewModel)
            /*createRow1()
            createRow2()*/
            /*createRow2()
            createRow3()
            createRow4()*/
        }
        //createRow1()
    }

    override fun onResume() {
        super.onResume()
        notifyReady()
    }

    private fun notifyReady() {
        mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            Log.e("kekeke", "onItemSelected $rowViewHolder, $item")
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