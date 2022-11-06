package ru.radiationx.anilibria.presentation.feed

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.*
import ru.radiationx.anilibria.model.loading.*
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.feed.FeedDataState
import ru.radiationx.anilibria.ui.fragments.feed.FeedScheduleState
import ru.radiationx.anilibria.ui.fragments.feed.FeedScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FeedRepository
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.*
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

/* Created by radiationx on 05.11.17. */

@InjectViewState
class FeedPresenter @Inject constructor(
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
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val updaterAnalytics: UpdaterAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val donationCardAnalytics: DonationCardAnalytics
) : BasePresenter<FeedView>(router) {

    companion object {
        private const val DONATION_NEW_TAG = "donation_new"
    }

    private val loadingController = DataLoadingController(presenterScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val stateController = StateController(FeedScreenState())

    private var randomJob: Job? = null

    private var lastLoadedPage: Int? = null

    private var appUpdateNeedClose = false
    private var hasAppUpdate = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        checkerRepository
            .observeUpdate()
            .onEach {
                hasAppUpdate = it.code > sharedBuildConfig.versionCode
                updateAppUpdateState()
            }
            .launchIn(presenterScope)

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
                stateController.updateState {
                    it.copy(donationCardItemState = newDonationState)
                }
            }
            .launchIn(presenterScope)

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

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
                stateController.updateState {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(presenterScope)

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
        releaseAnalytics.open(AnalyticsConstants.screen_feed, releaseItem.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        feedAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_feed, releaseItem.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onYoutubeClick(item: YoutubeItemState) {
        val youtubeItem = findYoutube(item.id) ?: return
        youtubeAnalytics.openVideo(AnalyticsConstants.screen_feed, youtubeItem.id, youtubeItem.vid)
        feedAnalytics.youtubeClick()
        Utils.externalLink(youtubeItem.link)
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
        randomJob = presenterScope.launch {
            runCatching {
                releaseInteractor.getRandomRelease()
            }.onSuccess {
                releaseAnalytics.open(AnalyticsConstants.screen_feed, null, it.code)
                router.navigateTo(Screens.ReleaseDetails(code = it.code))
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onFastSearchOpen() {
        fastSearchAnalytics.open(AnalyticsConstants.screen_feed)
    }

    fun appUpdateClick() {
        updaterAnalytics.appUpdateCardClick()
        router.navigateTo(Screens.AppUpdateScreen(false, AnalyticsConstants.app_update_card))
    }

    fun appUpdateCloseClick() {
        updaterAnalytics.appUpdateCardCloseClick()
        appUpdateNeedClose = true
        updateAppUpdateState()
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

    private fun updateAppUpdateState() {
        stateController.updateState {
            it.copy(hasAppUpdate = hasAppUpdate && !appUpdateNeedClose)
        }
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_feed, item.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_feed, item.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        ShortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_feed, item.id)
    }

    private fun findScheduleRelease(id: Int): Release? {
        val scheduleItems = loadingController.currentState.data?.schedule?.items
        return scheduleItems?.find { it.releaseItem.id == id }?.releaseItem
    }

    private fun findRelease(id: Int): Release? {
        val feedItems = loadingController.currentState.data?.feedItems
        return feedItems?.mapNotNull { it.release }?.find { it.id == id }
    }

    private fun findYoutube(id: Int): YoutubeItem? {
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

    private fun FeedData.toState(updates: Map<Int, ReleaseUpdate>): FeedDataState = FeedDataState(
        feedItems.map { it.toState(updates) },
        schedule?.toState()
    )

    private fun FeedScheduleData.toState(): FeedScheduleState = FeedScheduleState(
        title = title,
        items = items.map { it.toState() }
    )
}
