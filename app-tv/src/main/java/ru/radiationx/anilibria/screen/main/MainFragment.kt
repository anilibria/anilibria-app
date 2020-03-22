package ru.radiationx.anilibria.screen.main

import android.annotation.SuppressLint
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
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.GridScreen
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.shared.ktx.android.subscribeTo
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
                if (item is LinkCard) {
                    viewMode?.onLinkCardClick()
                } else if (item is LoadingCard) {
                    viewMode?.onLoadingCardClick()
                } else if (item is LibriaCard) {
                    viewMode?.onLibriaCardClick(item)
                } else {
                    router.navigateTo(GridScreen())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (rowsAdapter.size() == 0) {
            createRowBy(FEED_ROW_ID, feedViewModel)
            createRowBy(SCHEDULE_ROW_ID, scheduleViewModel)
            createRowBy(YOUTUBE_ROW_ID, youtubeViewModel)
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

    private fun notifyReady(){
        mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)

    }

    private fun getLoadDelay() = if (instantLoading) {
        0L
    } else {
        (100..3000).random().toLong()
    }

    private val diffCallback = object : DiffCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == Boolean
        }

    }

    private fun createRowBy(rowId: Long, viewModel: BaseCardsViewModel) {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector)
        val row = ListRow(rowId, HeaderItem(viewModel.defaultTitle), adapter)
        rowsAdapter.add(row)
        subscribeTo(viewModel.cardsData) {
            adapter.setItems(it, diffCallback)
        }
        subscribeTo(viewModel.rowTitle) {
            val position = rowsAdapter.indexOf(row)
            row.headerItem = HeaderItem(it)
            rowsAdapter.notifyArrayItemRangeChanged(position, 1)
        }
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