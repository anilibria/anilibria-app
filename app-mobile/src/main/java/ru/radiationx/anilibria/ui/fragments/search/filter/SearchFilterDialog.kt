package ru.radiationx.anilibria.ui.fragments.search.filter

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import ru.radiationx.data.api.shared.filter.FieldType
import ru.radiationx.data.api.shared.filter.FilterData
import ru.radiationx.data.api.shared.filter.FilterForm
import ru.radiationx.data.api.shared.filter.FilterItem
import ru.radiationx.data.api.shared.filter.FormItem
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoaderState
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.common.DialogType
import taiwa.common.NestedTaiwa
import taiwa.dsl.ContentScopeMarker
import taiwa.dsl.TaiwaContentScope
import taiwa.dsl.TaiwaScope
import taiwa.dsl.TaiwaScopeMarker
import taiwa.lifecycle.Destroyable

class SearchFilterDialog(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: SearchFilterViewModel
) : Destroyable {

    private val dialog = NestedTaiwa(context, lifecycleOwner, DialogType.BottomSheet)

    private val genresAnchor = TaiwaAnchor.Id(FieldType.Genre)
    private val sortingAnchor = TaiwaAnchor.Id(FieldType.Sorting)
    private val yearAnchor = TaiwaAnchor.Id(FieldType.Year)

    init {
        dialog.addDelegate(yearsRangeEnvoy {
            viewModel.onYears(it)
        })
        dialog.addDelegate(contentPlaceholderEnvoy {
            viewModel.refresh()
        })

        dialog.setCloseListener {
            viewModel.onApply()
        }
    }

    override fun onDestroy() {
        dialog.onDestroy()
    }

    fun show() {
        dialog.show()
    }

    fun setForm(filterState: SingleLoaderState<FilterData>, form: FilterForm) {
        val filter = filterState.data

        if (filter == null) {
            dialog.setContent {
                body {
                    envoy(ContentPlaceholderState(filterState.loading, filterState.error))
                }
            }
            return
        }
        dialog.setContent {
            body {
                if (filter.fields.contains(FieldType.Genre)) {
                    section {
                        text(FieldType.Genre.toTitle())
                    }
                    item(FieldType.Genre) {
                        title("Укажите жанры")
                        if (form.genres.isEmpty()) {
                            value("Не выбрано")
                        } else {
                            value("Выбрано: ${form.genres.size}")
                        }
                        action(TaiwaAction.Anchor(genresAnchor))
                        forward()
                    }
                    divider()
                }

                if (filter.fields.contains(FieldType.ReleaseType)) {
                    shipsSection(
                        fieldType = FieldType.ReleaseType,
                        values = filter.types,
                        formValues = form.types,
                        onClick = { viewModel.onReleaseType(it) }
                    )
                    divider()
                }

                if (filter.fields.contains(FieldType.PublishStatus)) {
                    shipsSection(
                        fieldType = FieldType.PublishStatus,
                        values = filter.publishStatuses,
                        formValues = form.publishStatuses,
                        onClick = { viewModel.onPublishStatus(it) }
                    )
                    divider()
                }

                if (filter.fields.contains(FieldType.ProductionStatus)) {
                    shipsSection(
                        fieldType = FieldType.ProductionStatus,
                        values = filter.productionStatuses,
                        formValues = form.productionStatuses,
                        onClick = { viewModel.onProductionStatus(it) }
                    )
                    divider()
                }

                if (filter.fields.contains(FieldType.Sorting)) {
                    section {
                        text(FieldType.Sorting.toTitle())
                    }
                    item(FieldType.Sorting) {
                        title("Укажите сортировку")
                        val selectedSorting = filter.sortings.find { it.item == form.sorting }
                        if (selectedSorting == null) {
                            value("Не выбрано")
                        } else {
                            value(selectedSorting.title)
                        }
                        action(TaiwaAction.Anchor(sortingAnchor))
                        forward()
                    }
                    divider()
                }

                if (filter.fields.contains(FieldType.Season)) {
                    shipsSection(
                        fieldType = FieldType.Season,
                        values = filter.seasons,
                        formValues = form.seasons,
                        onClick = { viewModel.onSeason(it) }
                    )
                    divider()
                }

                if (filter.fields.contains(FieldType.YearsRange)) {
                    section {
                        text(FieldType.YearsRange.toTitle())
                    }
                    envoy(YearsRangeState(filter.years, form.yearsRange))
                    divider()
                }

                if (filter.fields.contains(FieldType.Year)) {
                    section {
                        text(FieldType.Year.toTitle())
                    }
                    item(FieldType.Year) {
                        title("Укажите года")
                        if (form.years.isEmpty()) {
                            value("Не выбрано")
                        } else {
                            value("Выбрано: ${form.years.size}")
                        }
                        action(TaiwaAction.Anchor(yearAnchor))
                        forward()
                    }
                    divider()
                }

                if (filter.fields.contains(FieldType.AgeRating)) {
                    shipsSection(
                        fieldType = FieldType.AgeRating,
                        values = filter.ageRatings,
                        formValues = form.ageRatings,
                        onClick = { viewModel.onAgeRatings(it) }
                    )
                    divider()
                }
            }

            footer {
                buttons {
                    button {
                        text("Применить")
                        action(TaiwaAction.Close)
                        onClick {
                            viewModel.onApply()
                        }
                    }
                    if (form.hasChanges()) {
                        button {
                            text("Сбросить")
                            onClick {
                                viewModel.onReset()
                            }
                        }
                    }
                }
            }
            if (filter.fields.contains(FieldType.Genre)) {
                nested(genresAnchor) {
                    createGenresContent(filter.genres, form.genres)
                }
            }
            if (filter.fields.contains(FieldType.Sorting)) {
                nested(sortingAnchor) {
                    createSortingContent(filter.sortings, form.sorting)
                }
            }
            if (filter.fields.contains(FieldType.Year)) {
                nested(yearAnchor) {
                    createYearContent(filter.years, form.years)
                }
            }
        }
    }

    @TaiwaScopeMarker
    @ContentScopeMarker
    private fun TaiwaContentScope.shipsSection(
        fieldType: FieldType,
        values: List<FilterItem.Value>,
        formValues: Set<FormItem.Value>,
        onClick: (FormItem.Value) -> Unit
    ) {
        section {
            text(fieldType.toTitle())
        }
        chips(fieldType) {
            values.forEach { value ->
                chip(value.item) {
                    text(value.title)
                    select(formValues.contains(value.item))
                    onClick {
                        onClick.invoke(value.item)
                    }
                }
            }
        }
    }

    private fun TaiwaScope.createGenresContent(
        genres: List<FilterItem.Genre>,
        selected: Set<FormItem.Genre>
    ) {
        backAction(TaiwaAction.Root)
        header {
            toolbar {
                title(FieldType.Genre.toTitle())
                withBack()
            }
        }
        body {
            chips(FieldType.Genre) {
                genres.forEach { genre ->
                    chip(genre.item) {
                        text(genre.title)
                        select(selected.contains(genre.item))
                        onClick {
                            viewModel.onGenre(genre.item)
                        }
                    }
                }
            }
        }
        footer {
            buttons {
                button {
                    text("Применить")
                    action(TaiwaAction.Root)
                }
                if (selected.isNotEmpty()) {
                    button {
                        text("Сбросить жанры")
                        onClick {
                            viewModel.onResetGenres()
                        }
                    }
                }
            }
        }
    }


    private fun TaiwaScope.createYearContent(
        years: List<FilterItem.Year>,
        selected: Set<FormItem.Year>
    ) {
        backAction(TaiwaAction.Root)
        header {
            toolbar {
                title(FieldType.Year.toTitle())
                withBack()
            }
        }
        body {
            chips(FieldType.Year) {
                years.forEach { year ->
                    chip(year.item) {
                        text(year.title)
                        select(selected.contains(year.item))
                        onClick {
                            viewModel.onYear(year.item)
                        }
                    }
                }
            }
        }
        footer {
            buttons {
                button {
                    text("Применить")
                    action(TaiwaAction.Root)
                }
                if (selected.isNotEmpty()) {
                    button {
                        text("Сбросить периоды")
                        onClick {
                            viewModel.onResetYears()
                        }
                    }
                }
            }
        }
    }

    private fun TaiwaScope.createSortingContent(
        sorting: List<FilterItem.Value>?,
        selected: FormItem.Value?
    ) {
        backAction(TaiwaAction.Root)
        header {
            toolbar {
                title(FieldType.Sorting.toTitle())
                withBack()
            }
        }
        body {
            sorting?.forEach { sorting ->
                radioItem(sorting.item) {
                    title(sorting.title)
                    sorting.description?.also {
                        subtitle(it)
                    }
                    select(sorting.item == selected)
                    action(TaiwaAction.Root)
                    onClick {
                        viewModel.onSorting(sorting.item)
                    }
                }
            }
        }
        if (selected != null) {
            footer {
                buttons {
                    button {
                        text("Сбросить сортировку")
                        action(TaiwaAction.Root)
                        onClick {
                            viewModel.onResetSorting()
                        }
                    }
                }
            }
        }
    }


    private fun FieldType.toTitle(): String {
        return when (this) {
            FieldType.Query -> "Название аниме"
            FieldType.AgeRating -> "Возрастные рейтинги"
            FieldType.Genre -> "Жанры"
            FieldType.ProductionStatus -> "Статус озвучки"
            FieldType.PublishStatus -> "Статус выхода"
            FieldType.ReleaseType -> "Тип"
            FieldType.Season -> "Сезон"
            FieldType.Sorting -> "Сортировка"
            FieldType.Year -> "Период выхода"
            FieldType.YearsRange -> "Период выхода"
        }
    }
}