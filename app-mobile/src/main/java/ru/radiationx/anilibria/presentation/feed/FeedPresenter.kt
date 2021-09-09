package ru.radiationx.anilibria.presentation.feed

import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.applyAction
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

    private var randomDisposable = Disposables.disposed()
    private var dataDisposable = Disposables.disposed()

    private var lastLoadedPage: Int? = null

    private val currentItems = mutableListOf<FeedItem>()

    private var currentState = FeedScreenState()

    private var currentPage = Paginator.FIRST_PAGE

    private fun updateState(block: (FeedScreenState) -> FeedScreenState) {
        currentState = block.invoke(currentState)
        viewState.showState(currentState)
    }

    private fun updateStateByAction(action: ScreenStateAction<FeedDataState>) {
        updateState {
            it.copy(data = it.data.applyAction(action))
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

            val items = scheduleDays
                .firstOrNull { it.day == mskDay }
                ?.items
                ?.map { it.toState() }
                .orEmpty()

            FeedScheduleState(dayTitle, items)
        }

    private fun loadData(page: Int) {
        if (!dataDisposable.isDisposed) {
            return
        }
        if (lastLoadedPage != page) {
            feedAnalytics.loadPage(page)
            lastLoadedPage = page
        }
        val feedSource = getFeedSource(page)
        val isFirstPage = page == Paginator.FIRST_PAGE
        val isEmptyData = currentState.data.data == null
        val scheduleDataSource = if (isFirstPage) {
            getScheduleSource()
        } else {
            currentState.data.data?.schedule?.let { Single.just(it) } ?: getScheduleSource()
        }

        val action: ScreenStateAction<FeedDataState> = when {
            isFirstPage && isEmptyData -> ScreenStateAction.EmptyLoading()
            isFirstPage && !isEmptyData -> ScreenStateAction.Refresh()
            else -> ScreenStateAction.MoreLoading()
        }
        updateStateByAction(action)

        dataDisposable = Single
            .zip(
                feedSource,
                scheduleDataSource,
                BiFunction { feedItems: List<FeedItem>, scheduleState: FeedScheduleState ->
                    Pair(feedItems, scheduleState)
                }
            )
            .subscribe({ (feedItems, scheduleState) ->
                val feedDataState = FeedDataState(
                    feedItems = currentItems.map { it.toState() },
                    schedule = scheduleState
                )
                val action = ScreenStateAction.Data(feedDataState, feedItems.isNotEmpty())
                updateStateByAction(action)
                currentPage = page
            }, { throwable ->
                if (page == Paginator.FIRST_PAGE) {
                    errorHandler.handle(throwable)
                }
                updateStateByAction(ScreenStateAction.Error(throwable))
            })
            .addToDisposable()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshReleases()


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

                val newFeedItems = currentState.data.data?.feedItems?.map { feedItemState ->
                    val feedItem = itemsNeedUpdate.firstOrNull {
                        it.release?.id == feedItemState.release?.id
                                && it.youtube?.id == feedItemState.youtube?.id
                    }
                    feedItem?.toState() ?: feedItemState
                }.orEmpty()

                updateState {
                    it.copy(
                        data = it.data.copy(
                            data = it.data.data?.copy(
                                feedItems = newFeedItems
                            )
                        )
                    )
                }
            }
            .addToDisposable()
    }

    fun refreshReleases() {
        loadData(Paginator.FIRST_PAGE)
    }

    fun loadMore() {
        loadData(currentPage + 1)
    }

    private fun findRelease(id: Int): ReleaseItem? {
        return currentItems.mapNotNull { it.release }.firstOrNull { it.id == id }
    }

    private fun findYoutube(id: Int): YoutubeItem? {
        return currentItems.mapNotNull { it.youtube }.firstOrNull { it.id == id }
    }

    fun onScheduleScroll(position: Int) {
        feedAnalytics.scheduleHorizontalScroll(position)
    }

    fun onScheduleItemClick(item: ScheduleItemState, position: Int) {
        val releaseItem = findRelease(item.releaseId) ?: return
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
}
