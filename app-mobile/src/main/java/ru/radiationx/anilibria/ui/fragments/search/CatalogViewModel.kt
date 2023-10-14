package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.mapData
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.CatalogFilterAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

data class CatalogExtra(
    val genre: String?,
) : QuillExtra

@InjectConstructor
class CatalogViewModel(
    argExtra: CatalogExtra,
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
    private val systemUtils: SystemUtils,
) : ViewModel() {

    private var lastLoadedPage: Int? = null
    private val filterUseTimeCounter = TimeCounter()

    private val remindText =
        "Если не удаётся найти нужный релиз, попробуйте искать через Google или Yandex c приставкой \"AniLibria\".\nПо ссылке в поисковике можно будет открыть приложение."

    private val loadingController = DataLoadingController(viewModelScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }


    private val _filterState = MutableStateFlow(
        CatalogFilterState(
            form = SearchForm(
                genres = argExtra.genre?.let { setOf(GenreItem(it, it)) }.orEmpty(),
            )
        )
    )
    val filterState = _filterState.asStateFlow()

    private val _state = MutableStateFlow(SearchScreenState())
    val state = _state.asStateFlow()

    val showFilterAction = EventFlow<CatalogFilterState>()

    init {
        initGenres()
        initYears()
        initSeasons()
        observeSearchRemind()
        observeLoadingState()
        loadingController.refresh()
    }

    private fun initGenres() {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getGenres()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
        searchRepository
            .observeGenres()
            .onEach { genres ->
                _filterState.update { it.copy(genres = genres) }
            }
            .launchIn(viewModelScope)
    }

    private fun initYears() {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getYears()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
        searchRepository
            .observeYears()
            .onEach { years ->
                _filterState.update { it.copy(years = years) }
            }
            .launchIn(viewModelScope)
    }

    private fun initSeasons() {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getSeasons()
            }.onSuccess { seasons ->
                _filterState.update { it.copy(seasons = seasons) }
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun observeSearchRemind() {
        appPreferences
            .observeSearchRemind()
            .onEach { remindEnabled ->
                val newRemindText = remindText.takeIf { remindEnabled }
                _state.update {
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
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            catalogAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private suspend fun getDataSource(params: PageLoadParams): ScreenStateAction.Data<List<Release>> {
        return coRunCatching {
            val form = filterState.value.form
            searchRepository.searchReleases(form, params.page).let { paginated ->
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
        showFilterAction.set(filterState.value)
    }

    fun onAcceptDialog(state: CatalogFilterState) {
        catalogFilterAnalytics.applyClick()
        if (filterState.value != state) {
            _filterState.value = state
            refreshReleases()
        }
        _filterState.value = state
    }

    fun onCloseDialog() {
        catalogFilterAnalytics.useTime(filterUseTimeCounter.elapsed())
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

    private fun findRelease(id: ReleaseId): Release? {
        return loadingController.currentState.data?.find { it.id == id }
    }
}

data class CatalogFilterState(
    val genres: List<GenreItem> = emptyList(),
    val years: List<YearItem> = emptyList(),
    val seasons: List<SeasonItem> = emptyList(),
    val form: SearchForm = SearchForm(),
)