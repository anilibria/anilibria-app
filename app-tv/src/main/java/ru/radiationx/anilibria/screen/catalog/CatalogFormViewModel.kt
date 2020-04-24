package ru.radiationx.anilibria.screen.catalog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.search.SearchForm
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class CatalogFormViewModel(
    private val searchRepository: SearchRepository,
    private val catalogController: CatalogController
) : LifecycleViewModel() {

    val yearData = MutableLiveData<String>()
    val seasonData = MutableLiveData<String>()
    val genreData = MutableLiveData<String>()
    val sortData = MutableLiveData<String>()
    val onlyCompletedData = MutableLiveData<String>()

    private var searchForm = SearchForm()

    override fun onColdCreate() {
        super.onColdCreate()

        updateDataByForm()

        catalogController.yearsEvent.lifeSubscribe {
            searchForm = searchForm.copy(years = it)
            updateDataByForm()
        }
        catalogController.seasonsEvent.lifeSubscribe {
            searchForm = searchForm.copy(seasons = it)
            updateDataByForm()
        }
        catalogController.genresEvent.lifeSubscribe {
            searchForm = searchForm.copy(genres = it)
            updateDataByForm()
        }
        catalogController.sortEvent.lifeSubscribe {
            searchForm = searchForm.copy(sort = it)
            updateDataByForm()
        }
        catalogController.completedEvent.lifeSubscribe {
            searchForm = searchForm.copy(onlyCompleted = it)
            updateDataByForm()
        }
    }

    fun onYearClick() {

    }

    fun onSeasonClick() {

    }

    fun onGenreClick() {

    }

    fun onSortClick() {

    }

    fun onOnlyCompletedClick() {

    }

    private fun updateDataByForm() {
        Log.e("kokoko", "updateDataByForm $searchForm")
        yearData.value = searchForm.years.generateListTitle("За всё время")
        seasonData.value = searchForm.seasons?.map { it.title }.generateListTitle("Все сезоны")
        genreData.value = searchForm.genres?.map { it.title }.generateListTitle("Все жанры")
        sortData.value = when (searchForm.sort) {
            SearchForm.Sort.RATING -> "По популярности"
            SearchForm.Sort.DATE -> "По новизне"
        }
        onlyCompletedData.value = if (searchForm.onlyCompleted) {
            "Только завершенные"
        } else {
            "Все"
        }

        catalogController.applyFormEvent.accept(searchForm)
    }

    private fun List<String>?.generateListTitle(fallback: String): String {
        if (this == null || isEmpty()) {
            return fallback
        }
        var result = first()
        if (size > 1) {
            result += " +1"
        }
        return result
    }
}