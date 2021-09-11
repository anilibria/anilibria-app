package ru.radiationx.anilibria.presentation.search

import io.reactivex.Single
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.search.SearchScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.CatalogFilterAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class SearchPresenter @Inject constructor(
    private val searchRepository: SearchRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val catalogAnalytics: CatalogAnalytics,
    private val catalogFilterAnalytics: CatalogFilterAnalytics,
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val appPreferences: PreferencesHolder
) : BasePresenter<SearchCatalogView>(router) {


    private var lastLoadedPage: Int? = null
    private val filterUseTimeCounter = TimeCounter()

    private val remindText =
        "Если не удаётся найти нужный релиз, попробуйте искать через Google или Yandex c приставкой \"AniLibria\".\nПо ссылке в поисковике можно будет открыть приложение."

    private val staticSeasons = listOf("зима", "весна", "лето", "осень")
        .map { SeasonItem(it.capitalize(), it) }

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

    private val loadingController = DataLoadingController {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }.addToDisposable()
    private val stateController = StateController(SearchScreenState())

    private fun findRelease(id: Int): ReleaseItem? {
        return currentItems.find { it.id == id }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        observeScreenState()
        loadGenres()
        loadYears()
        observeGenres()
        observeYears()
        observeSearchRemind()
        updateInfo()
        viewState.showSeasons(staticSeasons)
        onChangeSorting(currentSorting)
        onChangeComplete(currentComplete)
        observeLoadingState()
        loadingController.refresh()
        releaseUpdateHolder
            .observeEpisodes()
            .subscribe { data ->
                val itemsNeedUpdate = mutableListOf<ReleaseItem>()
                currentItems.forEach { item ->
                    data.firstOrNull { it.id == item.id }?.also { updItem ->
                        val isNew =
                            item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                        if (item.isNew != isNew) {
                            item.isNew = isNew
                            itemsNeedUpdate.add(item)
                        }
                    }
                }

                val dataState = loadingController.currentState.data
                val newItems = dataState?.map { itemState ->
                    val releaseItem = itemsNeedUpdate.firstOrNull { it.id == itemState.id }
                    releaseItem?.toState() ?: itemState
                }
                loadingController.modifyData(newItems)
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

    private fun observeSearchRemind() {
        appPreferences
            .observeSearchRemind()
            .subscribe({ remindEnabled ->
                val newRemindText = remindText.takeIf { remindEnabled }
                stateController.updateState {
                    it.copy(remindText = newRemindText)
                }
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    private fun observeLoadingState() {
        loadingController
            .observeState()
            .subscribe { loadingData ->
                stateController.updateState {
                    it.copy(data = loadingData)
                }
            }
            .addToDisposable()
    }

    private fun observeScreenState() {
        stateController
            .observeState()
            .subscribe { viewState.showState(it) }
            .addToDisposable()
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            catalogAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private fun getDataSource(params: PageLoadParams): Single<ScreenStateAction.Data<List<ReleaseItemState>>> {
        val genresQuery = currentGenres.joinToString(",")
        val yearsQuery = currentYears.joinToString(",")
        val seasonsQuery = currentSeasons.joinToString(",")
        val completeStr = if (currentComplete) "2" else "1"
        return searchRepository
            .searchReleases(
                genresQuery,
                yearsQuery,
                seasonsQuery,
                currentSorting,
                completeStr,
                params.page
            )
            .map { paginated ->
                if (params.isFirstPage) {
                    currentItems.clear()
                }
                currentItems.addAll(paginated.data)

                val newItems = currentItems.map { it.toState() }
                ScreenStateAction.Data(newItems, paginated.data.isNotEmpty())
            }
            .doOnError {
                if (params.isFirstPage) {
                    errorHandler.handle(it)
                }
            }
    }

    fun refreshReleases() {
        loadingController.refresh()
    }

    fun loadMore() {
        loadingController.loadMore()
    }

    fun showDialog() {
        filterUseTimeCounter.start()
        catalogAnalytics.filterClick()
        catalogFilterAnalytics.open(AnalyticsConstants.screen_catalog)
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

    fun onAcceptDialog() {
        catalogFilterAnalytics.applyClick()
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

    fun onCloseDialog() {
        catalogFilterAnalytics.useTime(filterUseTimeCounter.elapsed())
        onAcceptDialog()
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
        viewState.updateInfo(
            currentSorting,
            currentGenres.size + currentYears.size + currentSeasons.size
        )
    }

    fun onFastSearchClick() {
        catalogAnalytics.fastSearchClick()
    }

    fun onFastSearchOpen() {
        fastSearchAnalytics.open(AnalyticsConstants.screen_catalog)
    }

    fun onRemindClose() {
        appPreferences.searchRemind = false
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        catalogAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_catalog, releaseItem.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_catalog, releaseItem.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_catalog, releaseItem.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        ShortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_catalog, releaseItem.id)
    }
}
