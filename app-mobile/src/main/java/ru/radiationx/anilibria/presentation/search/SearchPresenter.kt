package ru.radiationx.anilibria.presentation.search

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.*
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.search.SearchScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.CatalogFilterAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared_app.common.SystemUtils
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
    private val appPreferences: PreferencesHolder,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils
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

    private val beforeOpenDialogGenres = mutableListOf<String>()
    private val beforeOpenDialogYears = mutableListOf<String>()
    private val beforeOpenDialogSeasons = mutableListOf<String>()
    private var beforeOpenDialogSorting = ""
    private var beforeComplete = false

    private val loadingController = DataLoadingController(viewModelScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }
    private val stateController = StateController(SearchScreenState())

    private fun findRelease(id: ReleaseId): Release? {
        return loadingController.currentState.data?.find { it.id == id }
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
    }

    private fun loadGenres() {
        viewModelScope.launch {
            runCatching {
                searchRepository.getGenres()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun observeGenres() {
        searchRepository
            .observeGenres()
            .onEach {
                viewState.showGenres(it)
            }
            .launchIn(viewModelScope)
    }

    private fun loadYears() {
        viewModelScope.launch {
            runCatching {
                searchRepository.getYears()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun observeYears() {
        searchRepository
            .observeYears()
            .onEach {
                viewState.showYears(it)
            }
            .launchIn(viewModelScope)
    }

    private fun observeSearchRemind() {
        appPreferences
            .observeSearchRemind()
            .onEach { remindEnabled ->
                val newRemindText = remindText.takeIf { remindEnabled }
                stateController.update {
                    it.copy(remindText = newRemindText)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLoadingState() {
        combine(
            loadingController.observeState(),
            releaseUpdateHolder.observeEpisodes()
        ) { loadingState, updates ->
            val updatesMap = updates.associateBy { it.id }
            loadingState.mapData { items ->
                items.map { it.toState(updatesMap) }
            }
        }
            .onEach { loadingState ->
                stateController.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeScreenState() {
        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(viewModelScope)
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            catalogAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private suspend fun getDataSource(params: PageLoadParams): ScreenStateAction.Data<List<Release>> {
        val genresQuery = currentGenres.joinToString(",")
        val yearsQuery = currentYears.joinToString(",")
        val seasonsQuery = currentSeasons.joinToString(",")
        val completeStr = if (currentComplete) "2" else "1"
        return runCatching {
            searchRepository
                .searchReleases(
                    genresQuery,
                    yearsQuery,
                    seasonsQuery,
                    currentSorting,
                    completeStr,
                    params.page
                )
                .let { paginated ->
                    val newItems = if (params.isFirstPage) {
                        paginated.data
                    } else {
                        loadingController.currentState.data.orEmpty() + paginated.data
                    }
                    ScreenStateAction.Data(newItems, paginated.data.isNotEmpty())
                }
        }.onFailure {
            if (params.isFirstPage) {
                errorHandler.handle(it)
            }
        }.getOrThrow()
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
        releaseAnalytics.open(AnalyticsConstants.screen_catalog, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }
}
