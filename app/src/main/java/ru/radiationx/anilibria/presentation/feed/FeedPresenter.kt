package ru.radiationx.anilibria.presentation.feed

import com.arellomobile.mvp.InjectViewState
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.extension.isSameDay
import ru.radiationx.anilibria.model.repository.FeedRepository
import ru.radiationx.anilibria.model.repository.ScheduleRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.Paginator
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

/* Created by radiationx on 05.11.17. */

@InjectViewState
class FeedPresenter @Inject constructor(
        private val feedRepository: FeedRepository,
        private val scheduleRepository: ScheduleRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<FeedView>(router) {

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
                        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        val day = ScheduleDay.fromCalendarDay(calendarDay)

                        val items = it.second.firstOrNull { it.day == day }?.items ?: emptyList()

                        val feedSchedule = items.map {
                            val updTime = it.torrentUpdate
                            val millisTime = (updTime.toLong() * 1000L)
                            val updDate = Calendar.getInstance().also {
                                it.timeInMillis = millisTime
                            }
                            val isSameDay = updDate.time.isSameDay(Date())
                            FeedScheduleItem(it, calendarDay == updDate.get(Calendar.DAY_OF_WEEK) && isSameDay)
                        }
                        viewState.showSchedules(feedSchedule.sortedWith(compareByDescending<FeedScheduleItem> { it.completed }.then(compareByDescending { it.releaseItem.torrentUpdate })))
                    }
                    .map { it.first }
        } else {
            feedRepository.getFeed(page)
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshReleases()
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

    fun onItemClick(item: ReleaseItem) {
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onSchedulesClick() {
        router.navigateTo(Screens.Schedule())
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
