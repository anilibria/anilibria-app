package ru.radiationx.anilibria.ui.fragments.feed

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.radiationx.anilibria.ads.NativeAdItem
import ru.radiationx.anilibria.ads.NativeAdsRepository
import ru.radiationx.anilibria.ads.addAdAt
import ru.radiationx.anilibria.ads.convert
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.mapData
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.ads.AdsConfigRepository
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.DonationCardAnalytics
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.FeedAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.analytics.features.YoutubeAnalytics
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
import ru.radiationx.shared.ktx.asDayNameDeclension
import ru.radiationx.shared.ktx.asDayPretext
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared.ktx.getDayOfWeek
import ru.radiationx.shared.ktx.isSameDay
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.InjectConstructor
import java.util.Date
import java.util.Locale

/* Created by radiationx on 05.11.17. */

@OptIn(ExperimentalCoroutinesApi::class)
@InjectConstructor
class FeedViewModel(
    private val nativeAdsRepository: NativeAdsRepository,
    private val adsConfigRepository: AdsConfigRepository,
    private val feedRepository: FeedRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val scheduleRepository: ScheduleRepository,
    checkerRepository: CheckerRepository,
    releaseUpdateHolder: ReleaseUpdateHolder,
    private val appPreferences: PreferencesHolder,
    private val donationRepository: DonationRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils,
    permissionsController: MintPermissionsController,
    private val permissionsDialogFlow: MintPermissionsDialogFlow,
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val updaterAnalytics: UpdaterAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val donationCardAnalytics: DonationCardAnalytics,
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
                if (it.hasUpdate) {
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
            coRunCatching {
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
        val feedItems = loadingController.currentState.data?.feedItems ?: return null
        return feedItems
            .filterIsInstance<NativeAdItem.Data<FeedItem>>()
            .mapNotNull { it.data.release }
            .find { it.id == id }
    }

    private fun findYoutube(id: YoutubeId): YoutubeItem? {
        val feedItems = loadingController.currentState.data?.feedItems ?: return null
        return feedItems
            .filterIsInstance<NativeAdItem.Data<FeedItem>>()
            .mapNotNull { it.data.youtube }
            .find { it.id == id }
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
                val dayName = mskDay.asDayNameDeclension().lowercase(Locale.getDefault())
                "Ожидается $preText $dayName (по МСК)"
            }

            val items = scheduleDays
                .firstOrNull { it.day == mskDay }
                ?.items
                .orEmpty()

            FeedScheduleData(dayTitle, items)
        }


    private suspend fun getDataSource(params: PageLoadParams): ScreenStateAction.Data<FeedData> {
        return supervisorScope {
            val adsConfig = adsConfigRepository.getConfig().feedNative
            val newPageAsync = async { getFeedSource(params.page) }
            val scheduleAsync = async {
                if (params.isFirstPage) {
                    getScheduleSource()
                } else {
                    loadingController.currentState.data?.schedule ?: getScheduleSource()
                }
            }
            val adsAsync = async {
                withTimeout(adsConfig.timeoutMillis) {
                    if (adsConfig.enabled) {
                        val request = NativeAdRequestConfiguration
                            .Builder(adsConfig.unitId)
                            .setContextTags(adsConfig.contextTags)
                            .build()
                        nativeAdsRepository.load(request)
                    } else {
                        null
                    }
                }
            }

            coRunCatching {
                val newPage = newPageAsync.await()
                val schedule = scheduleAsync.await()
                val ad = runCatching { adsAsync.await() }
                    .onFailure { Timber.e(it) }
                    .getOrNull()

                val newPageWithAds = newPage.addAdAt(adsConfig.listInsertPosition, ad)
                val newFeedItems = if (params.isFirstPage) {
                    newPageWithAds
                } else {
                    loadingController.currentState.data?.feedItems.orEmpty() + newPageWithAds
                }
                val feedDataState = FeedData(
                    feedItems = newFeedItems,
                    schedule = schedule
                )
                ScreenStateAction.Data(feedDataState, newPage.isNotEmpty())
            }.onFailure {
                if (params.isFirstPage) {
                    errorHandler.handle(it)
                }
            }.getOrThrow()
        }
    }

    private data class FeedData(
        val feedItems: List<NativeAdItem<FeedItem>> = emptyList(),
        val schedule: FeedScheduleData? = null,
    )

    private data class FeedScheduleData(
        val title: String,
        val items: List<ScheduleItem>,
    )

    private fun FeedData.toState(updates: Map<ReleaseId, ReleaseUpdate>): FeedDataState =
        FeedDataState(
            feedItems.map { adItem ->
                adItem.convert { it.toState(updates) }
            },
            schedule?.toState()
        )

    private fun FeedScheduleData.toState(): FeedScheduleState = FeedScheduleState(
        title = title,
        items = items.map { it.toState() }
    )
}
