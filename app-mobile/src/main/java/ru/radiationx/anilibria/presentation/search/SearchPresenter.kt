package ru.radiationx.anilibria.presentation.search

import android.util.Log
import io.reactivex.disposables.Disposables
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.applyAction
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.Paginator
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

    companion object {
        private const val START_PAGE = 1
    }

    private var lastLoadedPage: Int? = null
    private val filterUseTimeCounter = TimeCounter()

    private val remindText =
        "Если не удаётся найти нужный релиз, попробуйте искать через Google или Yandex c приставкой \"AniLibria\".\nПо ссылке в поисковике можно будет открыть приложение."

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

    private var dataDisposable = Disposables.disposed()

    private var currentState = SearchScreenState()

    private fun updateState(block: (SearchScreenState) -> SearchScreenState) {
        val newState = block.invoke(currentState)
        if (currentState != newState) {
            currentState = newState
            viewState.showState(currentState)
        }
    }

    private fun updateStateByAction(action: ScreenStateAction<List<ReleaseItemState>>) {
        updateState {
            it.copy(data = it.data.applyAction(action))
        }
    }

    private fun findRelease(id: Int): ReleaseItem? {
        return currentItems.find { it.id == id }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadGenres()
        loadYears()
        observeGenres()
        observeYears()
        observeSearchRemind()
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
                        val isNew =
                            item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                        if (item.isNew != isNew) {
                            item.isNew = isNew
                            itemsNeedUpdate.add(item)
                        }
                    }
                }

                val newItems = currentState.data.data?.map { itemState ->
                    val releaseItem = itemsNeedUpdate.firstOrNull { it.id == itemState.id }
                    releaseItem?.toState() ?: itemState
                }
                val action = ScreenStateAction.DataModify(newItems)
                updateStateByAction(action)
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
                updateState {
                    it.copy(remindText = newRemindText)
                }
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    private fun loadReleases(pageNum: Int) {
        if (!dataDisposable.isDisposed) {
            return
        }
        if (lastLoadedPage != pageNum) {
            catalogAnalytics.loadPage(pageNum)
            lastLoadedPage = pageNum
        }

        val isFirstPage = pageNum == START_PAGE
        val isEmptyData = currentState.data.data == null

        val action: ScreenStateAction<List<ReleaseItemState>> = when {
            isFirstPage && isEmptyData -> ScreenStateAction.EmptyLoading()
            isFirstPage && !isEmptyData -> ScreenStateAction.Refresh()
            else -> ScreenStateAction.MoreLoading()
        }
        updateStateByAction(action)

        val genresQuery = currentGenres.joinToString(",")
        val yearsQuery = currentYears.joinToString(",")
        val seasonsQuery = currentSeasons.joinToString(",")
        val completeStr = if (currentComplete) "2" else "1"
        dataDisposable = searchRepository
            .searchReleases(
                genresQuery,
                yearsQuery,
                seasonsQuery,
                currentSorting,
                completeStr,
                pageNum
            )
            .subscribe({ releaseItems ->
                if (pageNum == START_PAGE) {
                    currentItems.clear()
                }
                currentItems.addAll(releaseItems.data)

                val newItems = currentItems.map { it.toState() }
                val action = ScreenStateAction.Data(newItems, releaseItems.data.isNotEmpty())
                updateStateByAction(action)
                currentPage = pageNum
            }) { throwable ->
                if (pageNum == Paginator.FIRST_PAGE) {
                    errorHandler.handle(throwable)
                }
                updateStateByAction(ScreenStateAction.Error(throwable))
            }
            .addToDisposable()
    }

    fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    fun loadMore() {
        loadReleases(currentPage + 1)
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
