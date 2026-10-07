package ru.radiationx.anilibria.screen.details

import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnPreDraw
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ItemBridgeAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardItem
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaDetailsRow
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.RowDiffCallback
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.anilibria.ui.presenter.ReleaseDetailsPresenter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowViewHolder
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.QuillExtra
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.imageloader.showImageUrl
import kotlin.coroutines.resume
import kotlin.math.ceil

data class DetailExtra(
    val id: ReleaseId,
) : QuillExtra

class DetailFragment : RowsSupportFragment() {

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(releaseId: ReleaseId) = DetailFragment().putExtra {
            putParcelable(ARG_ID, releaseId)
        }
    }

    private val backgroundManager by inject<GradientBackgroundManager>()

    private var headerBackgroundImage: String? = null
    private val preparationRows = mutableSetOf<Long>()
    private val preparedRows = mutableMapOf<Long, PreparedRow>()
    private var preparationRecycler: RecyclerView.Recycler? = null
    private val rowPreparationMutex = Mutex()

    private data class PreparedRow(
        val row: ListRow,
        val position: Int,
        val headerName: String?,
        val items: List<Any?>,
        val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        val holder: RecyclerView.ViewHolder,
    )

    private val argExtra by lazy {
        DetailExtra(id = getExtraNotNull(ARG_ID))
    }

    private val rowsPresenter by lazy {
        ClassPresenterSelector().apply {
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            addClassPresenter(
                LibriaDetailsRow::class.java, ReleaseDetailsPresenter(
                    continueClickListener = headerViewModel::onContinueClick,
                    playClickListener = headerViewModel::onPlayClick,
                    favoriteClickListener = headerViewModel::onFavoriteClick,
                    descriptionClickListener = headerViewModel::onDescriptionClick,
                    otherClickListener = headerViewModel::onOtherClick
                )
            )
        }
    }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val detailsViewModel by viewModel<DetailsViewModel> { argExtra }

    private val headerViewModel by viewModel<DetailHeaderViewModel> { argExtra }

    private val relatedViewModel by viewModel<DetailRelatedViewModel> { argExtra }

    private val recommendsViewModel by viewModel<DetailRecommendsViewModel> { argExtra }

    private fun getViewModel(rowId: Long): ViewModel? = when (rowId) {
        DetailsViewModel.RELEASE_ROW_ID -> headerViewModel
        DetailsViewModel.RELATED_ROW_ID -> relatedViewModel
        DetailsViewModel.RECOMMENDS_ROW_ID -> recommendsViewModel
        else -> null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerBackgroundImage = null
        preparationRows.clear()
        // Row updates must not pre-bind offscreen cards during the header transition.
        verticalGridView?.itemAnimator = null
        verticalGridView?.setViewCacheExtension(object : RecyclerView.ViewCacheExtension() {
            override fun getViewForPositionAndType(
                recycler: RecyclerView.Recycler,
                position: Int,
                type: Int,
            ): View? {
                preparationRecycler = recycler
                if (position !in 0 until rowsAdapter.size()) return null
                val row = rowsAdapter.get(position) as? ListRow ?: return null
                val prepared = preparedRows.remove(row.id) ?: return null
                if (prepared.position != position || prepared.holder.itemViewType != type ||
                    prepared.holder.bindingAdapterPosition != position ||
                    prepared.adapter !== verticalGridView?.adapter || prepared.row !== row ||
                    prepared.headerName != row.headerItem?.name ||
                    prepared.items.size != row.adapter.size() ||
                    prepared.items.indices.any { prepared.items[it] !== row.adapter.get(it) }
                ) {
                    discardPreparedRow(prepared)
                    return null
                }
                if (ru.radiationx.anilibria.BuildConfig.DEBUG) {
                    android.util.Log.d("TVRowPreparation", "reuse position=$position bound=${prepared.holder.bindingAdapterPosition}")
                }
                return prepared.holder.itemView
            }
        })

        viewLifecycleOwner.lifecycle.addObserver(detailsViewModel)
        viewLifecycleOwner.lifecycle.addObserver(headerViewModel)
        viewLifecycleOwner.lifecycle.addObserver(relatedViewModel)
        viewLifecycleOwner.lifecycle.addObserver(recommendsViewModel)

        adapter = rowsAdapter

        setOnItemViewClickedListener { _, item, _, row ->
            val viewMode: BaseCardsViewModel? =
                getViewModel((row as ListRow).id) as? BaseCardsViewModel
            when (item) {
                is LinkCard -> viewMode?.onLinkCardClick()
                is LoadingCard -> viewMode?.onLoadingCardClick()
                is LibriaCard -> viewMode?.onLibriaCardClick(item)
            }
        }

        setOnItemViewSelectedListener { _, item, rowViewHolder, row ->
            if (row is ListRow) {
                headerBackgroundImage = null
                backgroundManager.applyCard(item)
            } else if (row is LibriaDetailsRow) {
                row.details?.image?.takeIf { it.isNotBlank() }?.let { image ->
                    if (headerBackgroundImage != image) {
                        headerBackgroundImage = image
                        applyImage(image)
                    }
                }
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
                        rowViewHolder.setDescription(item.title, item.description)
                    }

                    else -> {
                        rowViewHolder.setDescription("", "")
                    }
                }
            }
        }

        val rowMap = mutableMapOf<Long, Row>()
        subscribeTo(detailsViewModel.rowListData) { rowList ->
            val rows = rowList.map { rowId ->
                val row = rowMap[rowId] ?: createRowBy(rowId, rowsAdapter, getViewModel(rowId)!!)
                rowMap[rowId] = row
                row
            }
            rowsAdapter.setItems(rows, RowDiffCallback)
        }
    }

    override fun onDestroyView() {
        preparedRows.values.forEach(::discardPreparedRow)
        preparedRows.clear()
        preparationRecycler = null
        preparationRows.clear()
        verticalGridView?.setViewCacheExtension(null)
        super.onDestroyView()
    }

    private fun createRowBy(
        rowId: Long,
        rowsAdapter: ArrayObjectAdapter,
        viewModel: ViewModel,
    ): Row = when (rowId) {
        DetailsViewModel.RELEASE_ROW_ID -> createHeaderRowBy(
            rowId,
            rowsAdapter,
            viewModel as DetailHeaderViewModel
        )

        else -> createCardsRowBy(rowId, rowsAdapter, viewModel as BaseCardsViewModel).also { row ->
            subscribeTo(combine(viewModel.cardsData, headerViewModel.progressState) { cards, state ->
                cards.takeUnless { state.loadingProgress }.orEmpty()
            }) { cards -> prepareRow(row, cards) }
        }
    }

    private fun prepareRow(row: ListRow, cards: List<CardItem>) {
        val releaseCards = cards.filterIsInstance<LibriaCard>()
        if (releaseCards.isEmpty() || !preparationRows.add(row.id)) return
        viewLifecycleOwner.lifecycleScope.launch {
            rowPreparationMutex.withLock {
                val grid = verticalGridView ?: return@withLock
                val bridgeAdapter = grid.adapter ?: return@withLock
                val recycler = preparationRecycler ?: return@withLock
                awaitNextDraw(grid)
                val cardWidth = resources.getDimension(ru.radiationx.anilibria.R.dimen.card_release_width)
                val width = grid.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
                val count = ceil(width / cardWidth.toDouble()).toInt()
                    .coerceIn(1, 8).coerceAtMost(releaseCards.size + 1)
                val position = rowsAdapter.indexOf(row)
                if (position < 0) return@withLock
                val headerName = row.headerItem?.name
                val items = (0 until row.adapter.size()).map { row.adapter.get(it) }
                val layout = LinearLayoutManager(requireContext())
                val preparationGrid = RecyclerView(requireContext()).apply {
                    layoutManager = layout
                    itemAnimator = null
                    setHasFixedSize(true)
                    adapter = bridgeAdapter
                }
                try {
                    layout.scrollToPositionWithOffset(position, 0)
                    repeat(count) { step ->
                        awaitMainThreadIdle()
                        while (backgroundManager.isAnimating) {
                            delay(16L)
                            awaitMainThreadIdle()
                        }
                        if (!grid.isAttachedToWindow || grid.selectedPosition != 0 ||
                            rowsAdapter.indexOf(row) != position
                        ) return@withLock
                        // Increase the viewport one poster at a time, keeping real Leanback holders.
                        val stepWidth = if (step == count - 1) width else
                            ((step + 1) * cardWidth).toInt().coerceAtMost(width)
                        val selectionListener = onItemViewSelectedListener
                        setOnItemViewSelectedListener(null)
                        try {
                            preparationGrid.findViewHolderForAdapterPosition(position)
                                ?.itemView?.requestLayout()
                            preparationGrid.measure(
                                View.MeasureSpec.makeMeasureSpec(stepWidth, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(1, View.MeasureSpec.EXACTLY),
                            )
                            preparationGrid.layout(0, 0, stepWidth, 1)
                            if (step == 0) {
                                val holder = preparationGrid.findViewHolderForAdapterPosition(position)
                                    ?: return@withLock
                                // Give the holder to its destination Recycler before retaining the bind.
                                layout.removeView(holder.itemView)
                                recycler.bindViewToPosition(holder.itemView, position)
                                layout.addView(holder.itemView)
                                val bridgeHolder = holder as ItemBridgeAdapter.ViewHolder
                                val presenter = bridgeHolder.presenter as RowPresenter
                                val rowHolder = presenter.getRowViewHolder(bridgeHolder.viewHolder)
                                    as CustomListRowViewHolder
                                rowHolder.setDescription(releaseCards.first().title, releaseCards.first().description)
                                presenter.setRowViewSelected(bridgeHolder.viewHolder, true)
                                preparationGrid.requestLayout()
                            }
                        } finally {
                            // Offscreen preparation must not select a card or recolor the visible screen.
                            setOnItemViewSelectedListener(selectionListener)
                        }
                        if (ru.radiationx.anilibria.BuildConfig.DEBUG) {
                            val bridge = preparationGrid.findViewHolderForAdapterPosition(position) as? ItemBridgeAdapter.ViewHolder
                            val rowHolder = (bridge?.presenter as? RowPresenter)?.getRowViewHolder(bridge.viewHolder) as? CustomListRowViewHolder
                            android.util.Log.d("TVRowPreparation", "step=$step width=$stepWidth row=${bridge?.itemView?.width} inner=${rowHolder?.gridView?.width} cards=${rowHolder?.gridView?.childCount}")
                        }
                        delay(16L)
                    }
                    val holder = preparationGrid.findViewHolderForAdapterPosition(position)
                        ?: return@withLock
                    layout.removeView(holder.itemView)
                    preparedRows[row.id] = PreparedRow(
                        row, position, headerName, items, bridgeAdapter, holder,
                    )
                } finally {
                    for (index in 0 until preparationGrid.childCount) {
                        clearRowImages(preparationGrid.getChildViewHolder(preparationGrid.getChildAt(index)))
                    }
                    preparationGrid.adapter = null
                }
            }
        }
    }

    private fun discardPreparedRow(prepared: PreparedRow) {
        clearRowImages(prepared.holder)
        prepared.adapter.onViewRecycled(prepared.holder)
    }

    private fun clearRowImages(holder: RecyclerView.ViewHolder) {
        val bridgeHolder = holder as? ItemBridgeAdapter.ViewHolder ?: return
        val presenter = bridgeHolder.presenter as? RowPresenter ?: return
        val rowHolder = presenter.getRowViewHolder(bridgeHolder.viewHolder)
            as? CustomListRowViewHolder ?: return
        val grid = rowHolder.gridView
        for (index in 0 until grid.childCount) {
            val card = grid.getChildViewHolder(grid.getChildAt(index)) as? ItemBridgeAdapter.ViewHolder
            (card?.viewHolder?.view as? ImageCardView)?.mainImageView?.showImageUrl(null)
        }
    }

    private suspend fun awaitMainThreadIdle() = suspendCancellableCoroutine<Unit> { continuation ->
        val queue = Looper.myQueue()
        val handler = MessageQueue.IdleHandler {
            if (continuation.isActive) continuation.resume(Unit)
            false
        }
        queue.addIdleHandler(handler)
        continuation.invokeOnCancellation { queue.removeIdleHandler(handler) }
    }

    private suspend fun awaitNextDraw(view: View) = suspendCancellableCoroutine<Unit> { continuation ->
        val listener = view.doOnPreDraw {
            if (continuation.isActive) continuation.resume(Unit)
        }
        continuation.invokeOnCancellation { listener.removeListener() }
        view.invalidate()
    }

    private fun createHeaderRowBy(
        rowId: Long,
        rowsAdapter: ArrayObjectAdapter,
        viewModel: DetailHeaderViewModel,
    ): Row {
        val row = LibriaDetailsRow(rowId)
        subscribeTo(viewModel.releaseData) {
            val position = rowsAdapter.indexOf(row)
            row.details = it
            rowsAdapter.notifyArrayItemRangeChanged(position, 1)
        }
        subscribeTo(viewModel.progressState) {
            val position = rowsAdapter.indexOf(row)
            row.state = it
            rowsAdapter.notifyArrayItemRangeChanged(position, 1)
        }
        return row
    }

    private fun applyImage(image: String) {
        backgroundManager.applyImage(image, colorModifier = {
            val hslColor = FloatArray(3)
            ColorUtils.colorToHSL(it, hslColor)
            hslColor[1] = (hslColor[1] + 0.05f).coerceAtMost(1.0f)
            hslColor[2] = (hslColor[2] + 0.05f).coerceAtMost(1.0f)
            ColorUtils.HSLToColor(hslColor)
        })
    }

}
