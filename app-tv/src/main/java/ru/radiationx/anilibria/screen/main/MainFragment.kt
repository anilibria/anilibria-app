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
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import ru.radiationx.shared_app.di.viewModelFromParent
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class MainFragment : ScopedRowsFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val mainViewModel by viewModel<MainViewModel>()

    private val feedViewModel by viewModel<FeedViewModel>()
    private val scheduleViewModel by viewModel<ScheduleViewModel>()
    private val favoritesViewModel by viewModel<MainFavoritesViewModel>()
    private val youtubeViewModel by viewModel<YouTubeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(mainViewModel)
        lifecycle.addObserver(feedViewModel)
        lifecycle.addObserver(scheduleViewModel)
        lifecycle.addObserver(favoritesViewModel)
        lifecycle.addObserver(youtubeViewModel)

        Log.e("kekeke", "$this oncreate $savedInstanceState")

        adapter = rowsAdapter
        onItemViewSelectedListener = ItemViewSelectedListener()

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {
                Log.e("lalala", "onclick $item")
                val viewMode: BaseCardsViewModel? = getViewModel((row as ListRow).id)
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

    private fun getViewModel(rowId: Long): BaseCardsViewModel? = when (rowId) {
        MainViewModel.FEED_ROW_ID -> feedViewModel
        MainViewModel.SCHEDULE_ROW_ID -> scheduleViewModel
        MainViewModel.FAVORITE_ROW_ID -> favoritesViewModel
        MainViewModel.YOUTUBE_ROW_ID -> youtubeViewModel
        else -> null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rowMap = mutableMapOf<Long, ListRow>()
        subscribeTo(mainViewModel.rowListData) { rowList ->
            val rows = rowList.map { rowId ->
                val row = rowMap[rowId] ?: createCardsRowBy(rowId, rowsAdapter, getViewModel(rowId)!!)
                rowMap[rowId] = row
                row
            }
            rowsAdapter.setItems(rows, ListRowDiffCallback)
        }
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