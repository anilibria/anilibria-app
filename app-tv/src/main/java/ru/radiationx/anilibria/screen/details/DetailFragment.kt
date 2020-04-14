package ru.radiationx.anilibria.screen.details

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModel
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.extension.createCardsRowBy
import ru.radiationx.anilibria.ui.presenter.ReleaseDetailsPresenter
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import javax.inject.Inject

class DetailFragment : ScopedRowsFragment() {

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(releaseId: Int) = DetailFragment().putExtra {
            putInt(ARG_ID, releaseId)
        }
    }

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val releaseId by lazy { arguments?.getInt(ARG_ID) ?: -1 }

    private val rowsPresenter by lazy {
        ClassPresenterSelector().apply {
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            addClassPresenter(
                LibriaDetailsRow::class.java, ReleaseDetailsPresenter(
                    continueClickListener = headerViewModel::onContinueClick,
                    playClickListener = headerViewModel::onPlayClick,
                    playWebClickListener = headerViewModel::onPlayWebClick,
                    favoriteClickListener = headerViewModel::onFavoriteClick,
                    descriptionClickListener = headerViewModel::onDescriptionClick
                )
            )
        }
    }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val detailsViewModel by viewModel<DetailsViewModel>()
    private val headerViewModel by viewModel<DetailHeaderViewModel>()
    private val relatedViewModel by viewModel<DetailRelatedViewModel>()
    private val recommendsViewModel by viewModel<DetailRecommendsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(detailsViewModel)
        lifecycle.addObserver(headerViewModel)
        lifecycle.addObserver(relatedViewModel)
        lifecycle.addObserver(recommendsViewModel)

        detailsViewModel.releaseId = releaseId
        headerViewModel.releaseId = releaseId
        relatedViewModel.releaseId = releaseId
        recommendsViewModel.releaseId = releaseId

        Log.e("kekeke", "$this oncreate $savedInstanceState")

        adapter = rowsAdapter

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            val viewMode: BaseCardsViewModel? = getViewModel((row as ListRow).id) as? BaseCardsViewModel
            when (item) {
                is LinkCard -> viewMode?.onLinkCardClick()
                is LoadingCard -> viewMode?.onLoadingCardClick()
                is LibriaCard -> viewMode?.onLibriaCardClick(item)
            }
        }

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.e("kekeke", "select $item, $row")
            if (row is ListRow) {
                backgroundManager.applyCard(item)
            } else if (row is LibriaDetailsRow) {
                applyImage(row.details?.image.orEmpty())
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
    }

    private fun getViewModel(rowId: Long): ViewModel? = when (rowId) {
        DetailsViewModel.RELEASE_ROW_ID -> headerViewModel
        DetailsViewModel.RELATED_ROW_ID -> relatedViewModel
        DetailsViewModel.RECOMMENDS_ROW_ID -> recommendsViewModel
        else -> null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun createRowBy(rowId: Long, rowsAdapter: ArrayObjectAdapter, viewModel: ViewModel): Row = when (rowId) {
        DetailsViewModel.RELEASE_ROW_ID -> createHeaderRowBy(rowId, rowsAdapter, viewModel as DetailHeaderViewModel)
        else -> createCardsRowBy(rowId, rowsAdapter, viewModel as BaseCardsViewModel)
    }

    private fun createHeaderRowBy(rowId: Long, rowsAdapter: ArrayObjectAdapter, viewModel: DetailHeaderViewModel): Row {
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
        backgroundManager.applyImage(image, colorSelector = {
            val swatch = it.darkVibrantSwatch ?: it.vibrantSwatch
            val color = swatch?.rgb
            Log.e("kekeke", "apply detail $color")
            color
            null
        }) {
            val hslColor = FloatArray(3)
            ColorUtils.colorToHSL(it, hslColor)
            hslColor[1] = (hslColor[1] + 0.05f).coerceAtMost(1.0f)
            hslColor[2] = (hslColor[2] + 0.05f).coerceAtMost(1.0f)
            ColorUtils.HSLToColor(hslColor)
        }
    }

}