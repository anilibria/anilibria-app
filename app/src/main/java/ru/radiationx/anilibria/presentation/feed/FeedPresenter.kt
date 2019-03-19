package ru.radiationx.anilibria.presentation.feed

import com.arellomobile.mvp.InjectViewState
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
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
        private val releaseInteractor: ReleaseInteractor,
        private val scheduleRepository: ScheduleRepository,
        private val releaseUpdateHolder: ReleaseUpdateHolder,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<FeedView>(router) {

    private var randomDisposable = Disposables.disposed()

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
                        val items = it.second.firstOrNull { it.day == calendarDay }?.items
                        items?.also { viewState.showSchedules(it) }
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

    fun onItemClick(item: ReleaseItem) {
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onSchedulesClick() {
        router.navigateTo(Screens.Schedule())
    }

    fun onRandomClick() {
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

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
