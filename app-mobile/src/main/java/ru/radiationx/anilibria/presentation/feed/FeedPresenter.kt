package ru.radiationx.anilibria.presentation.feed

import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.Paginator
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.feed.FeedDataState
import ru.radiationx.anilibria.ui.fragments.feed.FeedScheduleState
import ru.radiationx.anilibria.ui.fragments.feed.FeedScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.interactors.ReleaseInteractor
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
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<FeedView>(router) {

    private val loadingController = DataLoadingController {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }.addToDisposable()

    private val stateController = StateController(FeedScreenState())

    private var randomDisposable = Disposables.disposed()

    private var lastLoadedPage: Int? = null

    private val currentItems = mutableListOf<FeedItem>()
    private val currentScheduleItems = mutableListOf<ScheduleItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        stateController
            .observeState()
            .subscribe { viewState.showState(it) }
            .addToDisposable()

        loadingController.observeState()
            .subscribe { loadingState ->
                stateController.updateState {
                    it.copy(data = loadingState)
                }
            }
            .addToDisposable()

        loadingController.refresh()

        releaseUpdateHolder
            .observeEpisodes()
            .subscribe { data ->
                val itemsNeedUpdate = mutableListOf<FeedItem>()
                currentItems.forEach { item ->
                    data.firstOrNull { it.id == item.release?.id }?.also { updItem ->
                        val release = item.release!!
                        val isNew =
                            release.torrentUpdate > updItem.lastOpenTimestamp || release.torrentUpdate > updItem.timestamp
                        if (release.isNew != isNew) {
                            release.isNew = isNew
                            itemsNeedUpdate.add(item)
                        }
                    }
                }

                val dataState = loadingController.currentState.data
                val newFeedItems = dataState?.feedItems?.map { feedItemState ->
                    val feedItem = itemsNeedUpdate.firstOrNull {
                        it.release?.id == feedItemState.release?.id
                                && it.youtube?.id == feedItemState.youtube?.id
                    }
                    feedItem?.toState() ?: feedItemState
                }.orEmpty()

                loadingController.modifyData(dataState?.copy(feedItems = newFeedItems))
            }
            .addToDisposable()
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
        val releaseItem = currentScheduleItems
            .find { it.releaseItem.id == item.releaseId }
            ?.releaseItem ?: return
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
        if (!randomDisposable.isDisposed) {
            return
        }
        randomDisposable = releaseInteractor
            .getRandomRelease()
            .subscribe({
                releaseAnalytics.open(AnalyticsConstants.screen_feed, null, it.code)
                router.navigateTo(Screens.ReleaseDetails(code = it.code))
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun onFastSearchOpen() {
        fastSearchAnalytics.open(AnalyticsConstants.screen_feed)
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

    private fun findRelease(id: Int): ReleaseItem? {
        return currentItems.mapNotNull { it.release }.firstOrNull { it.id == id }
    }

    private fun findYoutube(id: Int): YoutubeItem? {
        return currentItems.mapNotNull { it.youtube }.firstOrNull { it.id == id }
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            feedAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private fun getFeedSource(page: Int): Single<List<FeedItem>> = feedRepository
        .getFeed(page)
        .doOnSuccess {
            if (page == Paginator.FIRST_PAGE) {
                currentItems.clear()
            }
            currentItems.addAll(it)
        }

    private fun getScheduleSource(): Single<FeedScheduleState> = scheduleRepository
        .loadSchedule()
        .map { scheduleDays ->
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

            currentScheduleItems.clear()
            val items = scheduleDays
                .firstOrNull { it.day == mskDay }
                ?.items
                ?.also { currentScheduleItems.addAll(it) }
                ?.map { it.toState() }
                .orEmpty()

            FeedScheduleState(dayTitle, items)
        }


    private fun getDataSource(params: PageLoadParams): Single<ScreenStateAction.Data<FeedDataState>> {
        val feedSource = getFeedSource(params.page)
        val scheduleDataSource = if (params.isFirstPage) {
            getScheduleSource()
        } else {
            loadingController.currentState.data?.schedule?.let { Single.just(it) }
                ?: getScheduleSource()
        }
        return Single
            .zip(
                feedSource,
                scheduleDataSource,
                BiFunction { feedItems: List<FeedItem>, scheduleState: FeedScheduleState ->
                    Pair(feedItems, scheduleState)
                }
            )
            .map { (feedItems, scheduleState) ->
                val feedDataState = FeedDataState(
                    feedItems = currentItems.map { it.toState() },
                    schedule = scheduleState
                )
                ScreenStateAction.Data(feedDataState, feedItems.isNotEmpty())
            }
            .doOnError {
                if (params.isFirstPage) {
                    errorHandler.handle(it)
                }
            }
    }
}
