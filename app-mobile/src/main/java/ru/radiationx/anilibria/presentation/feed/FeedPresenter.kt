package ru.radiationx.anilibria.presentation.feed

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.Paginator
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.FeedAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.schedule.ScheduleDay
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
        private val scheduleAnalytics: ScheduleAnalytics
) : BasePresenter<FeedView>(router) {

    private var randomDisposable = Disposables.disposed()

    private var lastLoadedPage:Int?=null

    private val currentItems = mutableListOf<FeedItem>()

    private val paginator = Paginator({
        loadFeed(it)/*.delay(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())*/
    }, object : Paginator.ViewController<FeedItem> {

        override fun showEmptyProgress(show: Boolean) {
            viewState.showEmptyProgress(show)
        }

        override fun showEmptyError(show: Boolean, error: Throwable?) {
            if (error != null) {
                errorHandler.handle(error) { ex, str ->
                    viewState.showEmptyError(show, str)
                }
            } else {
                viewState.showEmptyError(show, null)
            }
        }

        override fun showErrorMessage(error: Throwable) {
            errorHandler.handle(error)
        }

        override fun showEmptyView(show: Boolean) {
            viewState.showEmptyView(show)
        }

        override fun showData(show: Boolean, data: List<FeedItem>) {
            currentItems.clear()
            currentItems.addAll(data)
            viewState.showProjects(show, data)
        }

        override fun showRefreshProgress(show: Boolean) {
            viewState.showRefreshProgress(show)
        }

        override fun showPageProgress(show: Boolean) {
            viewState.showPageProgress(show)
        }

    })

    private fun loadFeed(page: Int): Single<List<FeedItem>> {
        if(lastLoadedPage!=page){
            feedAnalytics.loadPage(page)
            lastLoadedPage=page
        }
        return if (page == Paginator.FIRST_PAGE) {
            Single
                    .zip(
                            feedRepository.getFeed(page),
                            scheduleRepository.loadSchedule(),
                            BiFunction<List<FeedItem>, List<ScheduleDay>, Pair<List<FeedItem>, List<ScheduleDay>>> { t1, t2 ->
                                Pair(t1, t2)
                            }
                    )
                    .doOnSuccess {
                        val currentTime = System.currentTimeMillis()
                        val mskTime = System.currentTimeMillis().asMsk()

                        val currentDay = currentTime.getDayOfWeek()
                        val mskDay = mskTime.getDayOfWeek()


                        Log.d("ninini", "check correct = ${Date(currentTime)} :::: ${Date(mskTime)} ${Date(currentTime).isSameDay(Date(mskTime))}")

                        val dayTitle = if (Date(currentTime).isSameDay(Date(mskTime))) {
                            "Ожидается сегодня"
                        } else {
                            "Ожидается ${mskDay.asDayPretext()} ${mskDay.asDayNameDeclension().toLowerCase()} (по МСК)"
                        }

                        val items = it.second.firstOrNull { it.day == mskDay }?.items

                        items?.also {
                            viewState.showSchedules(dayTitle, it)
                        }
                    }
                    .map { it.first }
        } else {
            feedRepository.getFeed(page)
        }
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
                            val isNew = release.torrentUpdate > updItem.lastOpenTimestamp || release.torrentUpdate > updItem.timestamp
                            if (release.isNew != isNew) {
                                release.isNew = isNew
                                itemsNeedUpdate.add(item)
                            }
                        }
                    }

                    viewState.updateItems(itemsNeedUpdate)
                }
                .addToDisposable()
    }

    fun refreshReleases() {
        paginator.refresh()
    }

    fun loadMore() {
        paginator.loadNewPage()
    }

    override fun onDestroy() {
        super.onDestroy()
        paginator.release()
    }

    fun onScheduleScroll(position: Int){
        feedAnalytics.scheduleHorizontalScroll(position)
    }

    fun onScheduleItemClick(item: ReleaseItem,position:Int) {
        feedAnalytics.scheduleReleaseClick(position)
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onItemClick(item: ReleaseItem) {
        feedAnalytics.releaseClick()
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onYoutubeClick(youtubeItem:YoutubeItem){
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
                    router.navigateTo(Screens.ReleaseDetails(code = it.code))
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun onFastSearchOpen(){
        fastSearchAnalytics.open(AnalyticsConstants.screen_feed)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
