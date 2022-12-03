package ru.radiationx.anilibria.ui.fragments.feed

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.radiationx.anilibria.model.*
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.mapData
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.YoutubeId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FeedRepository
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.*
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.*

/* Created by radiationx on 05.11.17. */

@InjectConstructor
class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val scheduleRepository: ScheduleRepository,
    private val checkerRepository: CheckerRepository,
    private val sharedBuildConfig: SharedBuildConfig,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val appPreferences: PreferencesHolder,
    private val donationRepository: DonationRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils,
    private val permissionsController: MintPermissionsController,
    private val permissionsDialogFlow: MintPermissionsDialogFlow,
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val updaterAnalytics: UpdaterAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val donationCardAnalytics: DonationCardAnalytics
) : ViewModel() {

    companion object {
        private const val DONATION_NEW_TAG = "donation_new"
    }

    private val appUpdateWarning = FeedAppWarning(
        "update",
        "Доступно обновление приложения",
        FeedAppWarningType.INFO
    )

    private val appNotificationsWarning = FeedAppWarning(
        "notifications",
        "Приложению требуется разрешение на отправку уведомлений о новых сериях и обновлениях приложения",
        FeedAppWarningType.WARNING
    )

    private val loadingController = DataLoadingController(viewModelScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val _state = MutableStateFlow(FeedScreenState())
    val state = _state.asStateFlow()

    private val warningsController = AppWarningsController()

    private var randomJob: Job? = null

    private var lastLoadedPage: Int? = null

    init {

        checkerRepository
            .observeUpdate()
            .onEach {
                val hasAppUpdate = it.code > sharedBuildConfig.versionCode
                if (hasAppUpdate) {
                    warningsController.put(appUpdateWarning)
                } else {
                    warningsController.remove(appUpdateWarning.tag)
                }
            }
            .launchIn(viewModelScope)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsController
                .observe(Manifest.permission.POST_NOTIFICATIONS)
                .onEach {
                    if (!it.isGranted()) {
                        warningsController.put(appNotificationsWarning)
                    } else {
                        warningsController.remove(appNotificationsWarning.tag)
                    }
                }
                .launchIn(viewModelScope)
        }

        warningsController.warnings.onEach { warnings ->
            _state.update {
                it.copy(warnings = warnings.values.toList())
            }
        }.launchIn(viewModelScope)

        appPreferences
            .observeNewDonationRemind()
            .flatMapLatest { enabled ->
                donationRepository.observerDonationInfo().map {
                    Pair(it.cardNewDonations, enabled)
                }
            }
            .onEach { pair ->
                val newDonationState = if (pair.second) {
                    pair.first?.let {
                        DonationCardItemState(
                            tag = DONATION_NEW_TAG,
                            title = it.title,
                            subtitle = it.subtitle,
                            canClose = false
                        )
                    }
                } else {
                    null
                }
                _state.update {
                    it.copy(donationCardItemState = newDonationState)
                }
            }
            .launchIn(viewModelScope)

        combine(
            loadingController.observeState(),
            releaseUpdateHolder.observeEpisodes()
        ) { loadingState, updates ->
            val updatesMap = updates.associateBy { it.id }
            loadingState.mapData {
                it.toState(updatesMap)
            }
        }
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)

        loadingController.refresh()
    }

    fun refreshReleases() {
        loadingController.refresh()
    }

    fun loadMore() {
        loadingController.loadMore()
    }

    fun onScheduleScroll(position: Int) {
        feedAnalytics.scheduleHorizontalScroll(position)
    }

    fun onScheduleItemClick(item: ScheduleItemState, position: Int) {
        val releaseItem = findScheduleRelease(item.releaseId) ?: return
        feedAnalytics.scheduleReleaseClick(position)
        releaseAnalytics.open(AnalyticsConstants.screen_feed, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        feedAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_feed, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onYoutubeClick(item: YoutubeItemState) {
        val youtubeItem = findYoutube(item.id) ?: return
        youtubeAnalytics.openVideo(
            AnalyticsConstants.screen_feed,
            youtubeItem.id.id,
            youtubeItem.vid
        )
        feedAnalytics.youtubeClick()
        systemUtils.externalLink(youtubeItem.link)
    }

    fun onSchedulesClick() {
        scheduleAnalytics.open(AnalyticsConstants.screen_feed)
        feedAnalytics.scheduleClick()
        router.navigateTo(Screens.Schedule())
    }

    fun onRandomClick() {
        feedAnalytics.randomClick()
        if (randomJob?.isActive == true) {
            return
        }
        randomJob = viewModelScope.launch {
            runCatching {
                releaseInteractor.getRandomRelease()
            }.onSuccess {
                releaseAnalytics.open(AnalyticsConstants.screen_feed, null, it.code.code)
                router.navigateTo(Screens.ReleaseDetails(code = it.code))
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onFastSearchOpen() {
        fastSearchAnalytics.open(AnalyticsConstants.screen_feed)
    }

    fun appWarningClick(warning: FeedAppWarning) {
        when (warning.tag) {
            appUpdateWarning.tag -> {
                updaterAnalytics.appUpdateCardClick()
                val screen = Screens.AppUpdateScreen(false, AnalyticsConstants.app_update_card)
                router.navigateTo(screen)
            }
            appNotificationsWarning.tag -> {
                requestNotificationsPermission()
            }
        }
    }

    fun appWarningCloseClick(warning: FeedAppWarning) {
        when (warning.tag) {
            appUpdateWarning.tag -> {
                updaterAnalytics.appUpdateCardCloseClick()
            }
            appNotificationsWarning.tag -> {
                // do nothing
            }
        }
        warningsController.close(warning.tag)
    }

    fun onDonationClick(state: DonationCardItemState) {
        when (state.tag) {
            DONATION_NEW_TAG -> {
                donationCardAnalytics.onNewDonationClick(AnalyticsConstants.screen_feed)
                appPreferences.newDonationRemind = false
            }
        }
        donationDetailAnalytics.open(AnalyticsConstants.screen_feed)
        router.navigateTo(Screens.DonationDetail())
    }

    fun onDonationCloseClick(state: DonationCardItemState) {
        when (state.tag) {
            DONATION_NEW_TAG -> {
                donationCardAnalytics.onNewDonationCloseClick(AnalyticsConstants.screen_feed)
                appPreferences.newDonationRemind = false
            }
        }
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_feed, item.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_feed, item.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_feed, item.id.id)
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        viewModelScope.launch {
            permissionsDialogFlow.request(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun findScheduleRelease(id: ReleaseId): Release? {
        val scheduleItems = loadingController.currentState.data?.schedule?.items
        return scheduleItems?.find { it.releaseItem.id == id }?.releaseItem
    }

    private fun findRelease(id: ReleaseId): Release? {
        val feedItems = loadingController.currentState.data?.feedItems
        return feedItems?.mapNotNull { it.release }?.find { it.id == id }
    }

    private fun findYoutube(id: YoutubeId): YoutubeItem? {
        val feedItems = loadingController.currentState.data?.feedItems
        return feedItems?.mapNotNull { it.youtube }?.find { it.id == id }
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            feedAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private suspend fun getFeedSource(page: Int): List<FeedItem> = feedRepository.getFeed(page)

    private suspend fun getScheduleSource(): FeedScheduleData = scheduleRepository
        .loadSchedule()
        .let { scheduleDays ->
            val currentTime = System.currentTimeMillis()
            val mskTime = System.currentTimeMillis().asMsk()

            val mskDay = mskTime.getDayOfWeek()

            val asSameDay = Date(currentTime).isSameDay(Date(mskTime))
            val dayTitle = if (asSameDay) {
                "Ожидается сегодня"
            } else {
                val preText = mskDay.asDayPretext()
                val dayName = mskDay.asDayNameDeclension().toLowerCase()
                "Ожидается $preText $dayName (по МСК)"
            }

            val items = scheduleDays
                .firstOrNull { it.day == mskDay }
                ?.items
                .orEmpty()

            FeedScheduleData(dayTitle, items)
        }


    private suspend fun getDataSource(params: PageLoadParams): ScreenStateAction.Data<FeedData> {
        val feedSource = flow {
            val newPage = getFeedSource(params.page)
            val value = if (params.isFirstPage) {
                newPage
            } else {
                loadingController.currentState.data?.feedItems.orEmpty() + newPage
            }
            emit(value)
        }
        val scheduleDataSource = flow {
            val value = if (params.isFirstPage) {
                getScheduleSource()
            } else {
                loadingController.currentState.data?.schedule ?: getScheduleSource()
            }
            emit(value)
        }

        return combine(feedSource, scheduleDataSource) { feedItems, scheduleState ->
            Pair(feedItems, scheduleState)
        }
            .map { (feedItems, scheduleState) ->
                val feedDataState = FeedData(
                    feedItems = feedItems,
                    schedule = scheduleState
                )
                ScreenStateAction.Data(feedDataState, feedItems.isNotEmpty())
            }
            .catch {
                if (params.isFirstPage) {
                    errorHandler.handle(it)
                }
                throw it
            }
            .first()
    }

    private data class FeedData(
        val feedItems: List<FeedItem> = emptyList(),
        val schedule: FeedScheduleData? = null
    )

    private data class FeedScheduleData(
        val title: String,
        val items: List<ScheduleItem>
    )

    private fun FeedData.toState(updates: Map<ReleaseId, ReleaseUpdate>): FeedDataState =
        FeedDataState(
            feedItems.map { it.toState(updates) },
            schedule?.toState()
        )

    private fun FeedScheduleData.toState(): FeedScheduleState = FeedScheduleState(
        title = title,
        items = items.map { it.toState() }
    )
}
