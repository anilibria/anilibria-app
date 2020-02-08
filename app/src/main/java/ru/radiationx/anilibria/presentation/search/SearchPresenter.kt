package ru.radiationx.anilibria.presentation.search

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class SearchPresenter @Inject constructor(
        private val searchRepository: SearchRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) : BasePresenter<SearchView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private val staticSeasons = listOf("зима", "весна", "лето", "осень")
            .map { SeasonItem(it.capitalize(), it) }

    private var currentPage = START_PAGE
    private val currentGenres = mutableListOf<String>()
    private val currentYears = mutableListOf<String>()
    private val currentSeasons = mutableListOf<String>()
    private var currentSorting = "1"
    private var currentComplete = false
    private val currentItems = mutableListOf<ReleaseItem>()

    private val beforeOpenDialogGenres = mutableListOf<String>()
    private val beforeOpenDialogYears = mutableListOf<String>()
    private val beforeOpenDialogSeasons = mutableListOf<String>()
    private var beforeOpenDialogSorting = ""
    private var beforeComplete = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadGenres()
        loadYears()
        observeGenres()
        observeYears()
        updateInfo()
        viewState.showSeasons(staticSeasons)
        onChangeSorting(currentSorting)
        onChangeComplete(currentComplete)
        loadReleases(START_PAGE)
        releaseUpdateHolder
                .observeEpisodes()
                .subscribe { data ->
                    val itemsNeedUpdate = mutableListOf<ReleaseItem>()
                    currentItems.forEach { item ->
                        data.firstOrNull { it.id == item.id }?.also { updItem ->
                            val isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
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
        val seasonsQuery = currentSeasons.joinToString(",")
        val completeStr = if (currentComplete) "2" else "1"
        searchRepository
                .searchReleases(genresQuery, yearsQuery, seasonsQuery, currentSorting, completeStr, pageNum)
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
        beforeOpenDialogSeasons.clear()
        beforeOpenDialogGenres.addAll(currentGenres)
        beforeOpenDialogYears.addAll(currentYears)
        beforeOpenDialogSeasons.addAll(currentSeasons)
        beforeOpenDialogSorting = currentSorting
        beforeComplete = currentComplete
        viewState.showDialog()
    }

    fun onCloseDialog() {
        if (
                beforeOpenDialogGenres != currentGenres
                || beforeOpenDialogYears != currentYears
                || beforeOpenDialogSeasons != currentSeasons
                || beforeOpenDialogSorting != currentSorting
                || beforeComplete != currentComplete
        ) {
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

    fun onChangeSeasons(newSeasons: List<String>) {
        currentSeasons.clear()
        currentSeasons.addAll(newSeasons)
        viewState.selectSeasons(currentSeasons)
        updateInfo()
    }

    fun onChangeSorting(newSorting: String) {
        currentSorting = newSorting
        viewState.setSorting(currentSorting)
        updateInfo()
    }

    fun onChangeComplete(complete: Boolean) {
        currentComplete = complete
        viewState.setComplete(currentComplete)
        updateInfo()
    }

    private fun updateInfo() {
        viewState.updateInfo(currentSorting, currentGenres.size + currentYears.size + currentSeasons.size)
    }

    fun onItemClick(item: ReleaseItem) {
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
