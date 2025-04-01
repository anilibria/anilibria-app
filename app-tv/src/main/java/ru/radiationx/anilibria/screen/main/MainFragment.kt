package ru.radiationx.anilibria.screen.main

import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.RowDiffCallback
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowViewHolder
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.quillParentViewModel

class MainFragment : RowsSupportFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    // РАНЬШЕ БЫЛО: private val backgroundManager by inject<GradientBackgroundManager>()
    private val backgroundManager by lazy { GradientBackgroundManager(requireActivity()) }

    private val mainViewModel by quillParentViewModel<MainViewModel>()
    private val feedViewModel by quillParentViewModel<MainFeedViewModel>()
    private val scheduleViewModel by quillParentViewModel<MainScheduleViewModel>()
    private val favoritesViewModel by quillParentViewModel<MainFavoritesViewModel>()
    private val youtubeViewModel by quillParentViewModel<MainYouTubeViewModel>()

    private fun getViewModel(rowId: Long): BaseCardsViewModel? = when (rowId) {
        MainViewModel.FEED_ROW_ID -> feedViewModel
        MainViewModel.SCHEDULE_ROW_ID -> scheduleViewModel
        MainViewModel.FAVORITE_ROW_ID -> favoritesViewModel
        MainViewModel.YOUTUBE_ROW_ID -> youtubeViewModel
        else -> null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(mainViewModel)
        viewLifecycleOwner.lifecycle.addObserver(feedViewModel)
        viewLifecycleOwner.lifecycle.addObserver(scheduleViewModel)
        viewLifecycleOwner.lifecycle.addObserver(favoritesViewModel)
        viewLifecycleOwner.lifecycle.addObserver(youtubeViewModel)

        adapter = rowsAdapter
        onItemViewSelectedListener = ItemViewSelectedListener()

        setOnItemViewClickedListener { _, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {
                val vm: BaseCardsViewModel? = getViewModel((row as ListRow).id)
                when (item) {
                    is LinkCard -> vm?.onLinkCardClick()
                    is LoadingCard -> vm?.onLoadingCardClick()
                    is LibriaCard -> vm?.onLibriaCardClick(item)
                }
            }
        }

        val rowMap = mutableMapOf<Long, ListRow>()
        subscribeTo(mainViewModel.rowListData) { rowList ->
            val rows = rowList.map { rowId ->
                val row = rowMap[rowId]
                    ?: createCardsRowBy(rowId, rowsAdapter, getViewModel(rowId)!!)
                rowMap[rowId] = row
                row
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
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
            rowViewHolder: RowPresenter.ViewHolder, row: Row,
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
}