package ru.radiationx.anilibria.presentation.search

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.repository.SearchRepository
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

@InjectViewState
class SearchPresenter(
        private val searchRepository: SearchRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) : BasePresenter<SearchView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    private val currentGenres = mutableListOf<String>()
    private val currentYears = mutableListOf<String>()
    private var currentSorting = "2"

    private val currentItems = mutableListOf<ReleaseItem>()
    private val beforeOpenDialogGenres = mutableListOf<String>()
    private val beforeOpenDialogYears = mutableListOf<String>()
    private var beforeOpenDialogSorting = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach")
        loadGenres()
        loadYears()
        observeGenres()
        observeYears()
        updateInfo()
        onChangeSorting(currentSorting)
        loadReleases(START_PAGE)
        releaseUpdateHolder
                .observeEpisodes()
                .subscribe { data ->
                    val itemsNeedUpdate = mutableListOf<ReleaseItem>()
                    currentItems.forEach { item ->
                        data.firstOrNull { it.id == item.id }?.also { updItem ->
                            val isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                            Log.e("lalalupdata", "check pres ${item.id}, ${item.torrentUpdate} : ${updItem.id}, ${updItem.timestamp}, ${updItem.lastOpenTimestamp} : ${item.isNew}, $isNew")
                            if (item.isNew != isNew) {
                                item.isNew = isNew
                                itemsNeedUpdate.add(item)
                            }
                        }
                    }
                    viewState.updateReleases(itemsNeedUpdate)
                }
                .addToDisposable()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    fun fastSearch(query: String) {
        searchRepository
                .fastSearch(query)
                .subscribe({ searchItems ->
                    Log.d("S_DEF_LOG", "subscribe call show")
                    viewState.showFastItems(searchItems)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun loadGenres() {
        searchRepository
                .getGenres()
                .subscribe({ }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun observeGenres() {
        searchRepository
                .observeGenres()
                .subscribe({
                    viewState.showGenres(it)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun loadYears() {
        searchRepository
                .getYears()
                .subscribe({ }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun observeYears() {
        searchRepository
                .observeYears()
                .subscribe({
                    viewState.showYears(it)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun loadReleases(pageNum: Int) {
        Log.e("S_DEF_LOG", "loadReleases")

        /*if (isEmpty()) {
            viewState.setRefreshing(false)
            return
        }*/

        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        val genresQuery = currentGenres.joinToString(",")
        val yearsQuery = currentYears.joinToString(",")
        searchRepository
                .searchReleases(genresQuery, yearsQuery, currentSorting, pageNum)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ releaseItems ->
                    viewState.setEndless(releaseItems.data.isNotEmpty())
                    showData(releaseItems.data)
                }) {
                    if (currentItems.isEmpty()) {
                        showData(emptyList())
                    }
                    viewState.setEndless(false)
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun showData(data: List<ReleaseItem>) {
        updateInfo()
        if (isFirstPage()) {
            currentItems.clear()
            currentItems.addAll(data)
            viewState.showReleases(data)
        } else {
            currentItems.addAll(data)
            viewState.insertMore(data)
        }
    }

    fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    fun loadMore() {
        loadReleases(currentPage + 1)
    }

    fun showDialog() {
        beforeOpenDialogGenres.clear()
        beforeOpenDialogYears.clear()
        beforeOpenDialogGenres.addAll(currentGenres)
        beforeOpenDialogYears.addAll(currentYears)
        beforeOpenDialogSorting = currentSorting
        viewState.showDialog()
    }

    fun onCloseDialog() {
        if (beforeOpenDialogGenres != currentGenres || beforeOpenDialogYears != currentYears || beforeOpenDialogSorting != currentSorting) {
            refreshReleases()
        }
    }

    fun onChangeGenres(newGenres: List<String>) {
        currentGenres.clear()
        currentGenres.addAll(newGenres)
        viewState.selectGenres(currentGenres)
        updateInfo()
    }

    fun onChangeYears(newYears: List<String>) {
        currentYears.clear()
        currentYears.addAll(newYears)
        viewState.selectYears(currentYears)
        updateInfo()
    }

    fun onChangeSorting(newSorting: String) {
        currentSorting = newSorting
        viewState.setSorting(currentSorting)
        updateInfo()
    }

    private fun updateInfo() {
        viewState.updateInfo(currentSorting, currentGenres.size + currentYears.size)
    }

    fun onItemClick(item: ReleaseItem) {
        val args = Bundle()
        args.putInt(ReleaseFragment.ARG_ID, item.id)
        args.putString(ReleaseFragment.ARG_ID_CODE, item.code)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        router.navigateTo(Screens.RELEASE_DETAILS, args)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
