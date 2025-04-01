package ru.radiationx.anilibria.screen.details

import android.os.Bundle
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Row
import androidx.lifecycle.ViewModel
import ru.radiationx.anilibria.common.BaseCardsViewModel
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

/**
 * Простая data-класс «аргументов» для экрана.
 * Хранит releaseId и т.д.
 */
data class DetailExtra(
    val id: ReleaseId
) : QuillExtra

/**
 * Фрагмент, показывающий:
 *  1) «Шапку» (ReleaseDetails)
 *  2) «Related»/«Recommends» списки карточек
 */
class DetailFragment : RowsSupportFragment() {

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(releaseId: ReleaseId) = DetailFragment().putExtra {
            putParcelable(ARG_ID, releaseId)
        }
    }

    /** Менеджер фона (если хотите инъекцию — можно inject, но здесь lazy) */
    private val backgroundManager by lazy { GradientBackgroundManager(requireActivity()) }

    /** Аргументы */
    private val argExtra by lazy {
        DetailExtra(id = getExtraNotNull(ARG_ID))
    }

    /** Презентеры для строк/рядов */
    private val rowsPresenter by lazy {
        ClassPresenterSelector().apply {
            // Для обычного ListRow
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            // Для детали (LibriaDetailsRow)
            addClassPresenter(
                LibriaDetailsRow::class.java,
                ReleaseDetailsPresenter(
                    continueClickListener = { headerViewModel.onContinueClick() },
                    playClickListener = { headerViewModel.onPlayClick() },
                    favoriteClickListener = { headerViewModel.onFavoriteClick() },
                    descriptionClickListener = { headerViewModel.onDescriptionClick() },
                    otherClickListener = { headerViewModel.onOtherClick() }
                )
            )
        }
    }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    /** ViewModel’ы */
    private val detailsViewModel by viewModel<DetailsViewModel> { argExtra }
    private val headerViewModel by viewModel<DetailHeaderViewModel> { argExtra }
    private val relatedViewModel by viewModel<DetailRelatedViewModel> { argExtra }
    private val recommendsViewModel by viewModel<DetailRecommendsViewModel> { argExtra }

    /**
     * По rowId возвращаем ViewModel: либо headerViewModel, либо relatedViewModel/recommendsViewModel.
     * Если нужно что-то ещё — добавляйте case.
     */
    private fun getViewModel(rowId: Long): Any? = when (rowId) {
        DetailsViewModel.RELEASE_ROW_ID -> headerViewModel
        DetailsViewModel.RELATED_ROW_ID -> relatedViewModel
        DetailsViewModel.RECOMMENDS_ROW_ID -> recommendsViewModel
        else -> null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Привязываем lifecycle (чтобы onResume()/onPause() и др. вызывались)
        viewLifecycleOwner.lifecycle.addObserver(detailsViewModel)
        viewLifecycleOwner.lifecycle.addObserver(headerViewModel)
        viewLifecycleOwner.lifecycle.addObserver(relatedViewModel)
        viewLifecycleOwner.lifecycle.addObserver(recommendsViewModel)

        // Ставим адаптер
        adapter = rowsAdapter

        // Обработка кликов
        setOnItemViewClickedListener { _, item, _, row ->
            val vm = getViewModel((row as Row).id)
            // Проверяем, является ли vm «BaseCardsViewModel»
            if (vm is BaseCardsViewModel) {
                when (item) {
                    is LinkCard -> vm.onLinkCardClick()
                    is LoadingCard -> vm.onLoadingCardClick()
                    is LibriaCard -> vm.onLibriaCardClick(item)
                }
            }
        }

        // Выбор (focus) элемента
        setOnItemViewSelectedListener { _, item, rowViewHolder, row ->
            // Если это ListRow — используем applyCard(...) для фона
            if (row is ListRow) {
                backgroundManager.applyCard(item)
            }
            // Если это LibriaDetailsRow, вызовем applyImage(...) с его постером
            else if (row is LibriaDetailsRow) {
                val url = row.details?.image ?: ""
                applyImage(url)
            }

            // А ещё, если rowViewHolder — наш CustomListRowViewHolder, выставим description
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaCard -> rowViewHolder.setDescription(item.title, item.description)
                    is LinkCard -> rowViewHolder.setDescription(item.title, "")
                    is LoadingCard -> rowViewHolder.setDescription(item.title, item.description)
                    else -> rowViewHolder.setDescription("", "")
                }
            }
        }

        // Подписка на список rowId от detailsViewModel
        val rowMap = mutableMapOf<Long, Row>()
        subscribeTo(detailsViewModel.rowListData) { rowIds ->
            // rowIds обычно [1,2,3]
            val newRows = rowIds.map { rowId ->
                rowMap.getOrPut(rowId) { createRowBy(rowId) }
            }
            rowsAdapter.setItems(newRows, RowDiffCallback)
        }
    }

    /**
     * В зависимости от rowId делаем либо «шапку» (LibriaDetailsRow), либо «cards» (ListRow).
     */
    private fun createRowBy(rowId: Long): Row {
        return when (rowId) {
            DetailsViewModel.RELEASE_ROW_ID -> createHeaderRow(rowId, headerViewModel)
            DetailsViewModel.RELATED_ROW_ID,
            DetailsViewModel.RECOMMENDS_ROW_ID ->
                createCardsRowBy(rowId, rowsAdapter, getViewModel(rowId) as BaseCardsViewModel)

            else -> {
                // Фолбэк (пустая строка)
                ListRow(HeaderItem("Empty"), ArrayObjectAdapter())
            }
        }
    }

    /**
     * Для «шапки» (LibriaDetailsRow)
     */
    private fun createHeaderRow(rowId: Long, vm: DetailHeaderViewModel): Row {
        val row = LibriaDetailsRow(rowId)
        subscribeTo(vm.releaseData) {
            val pos = rowsAdapter.indexOf(row)
            row.details = it
            if (pos >= 0) rowsAdapter.notifyArrayItemRangeChanged(pos, 1)
        }
        subscribeTo(vm.progressState) {
            val pos = rowsAdapter.indexOf(row)
            row.state = it
            if (pos >= 0) rowsAdapter.notifyArrayItemRangeChanged(pos, 1)
        }
        return row
    }

    /**
     * Вызывается, когда фокус на LibriaDetailsRow → нужно обновить фон (по ссылке на картинку).
     */
    private fun applyImage(image: String) {
        backgroundManager.applyImage(
            image,
            colorSelector = { null } // Можно возвращать цвет, если хотите
        ) { originalColor ->
            // Немного модифицируем HSL
            val hslColor = FloatArray(3)
            ColorUtils.colorToHSL(originalColor, hslColor)
            hslColor[1] = (hslColor[1] + 0.05f).coerceAtMost(1.0f)
            hslColor[2] = (hslColor[2] + 0.05f).coerceAtMost(1.0f)
            ColorUtils.HSLToColor(hslColor)
        }
    }
}
