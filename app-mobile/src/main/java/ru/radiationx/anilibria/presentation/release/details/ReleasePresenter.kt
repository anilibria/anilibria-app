package ru.radiationx.anilibria.presentation.release.details

import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.HistoryRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter @Inject constructor(
        private val releaseInteractor: ReleaseInteractor,
        private val historyRepository: HistoryRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val commentsAnalytics: CommentsAnalytics
) : BasePresenter<ReleaseView>(router) {

    private var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(ReleaseFull(it))
        }
        observeRelease()
        loadRelease()
    }

    private fun loadRelease() {
        releaseInteractor
                .loadRelease(releaseId, releaseIdCode)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({
                    viewState.setRefreshing(false)
                    historyRepository.putRelease(it as ReleaseItem)
                }, {
                    viewState.setRefreshing(false)
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun observeRelease() {
        releaseInteractor
                .observeFull(releaseId, releaseIdCode)
                .subscribe({ release ->
                    updateLocalRelease(release)
                    historyRepository.putRelease(release as ReleaseItem)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun updateLocalRelease(release: ReleaseFull) {
        currentData = release
        releaseId = release.id
        releaseIdCode = release.code
        viewState.showRelease(release)
    }

    fun onShareClick() {
        currentData?.link?.let {
            viewState.shareRelease(it)
        }
    }

    fun onCopyLinkClick() {
        currentData?.link?.let {
            viewState.copyLink(it)
        }
    }

    fun onShortcutAddClick() {
        currentData?.let {
            viewState.addShortCut(it)
        }
    }

    fun onCommentsSwipe(){
        currentData?.also {
            commentsAnalytics.open(AnalyticsConstants.screen_release, it.id)
        }
    }

}
