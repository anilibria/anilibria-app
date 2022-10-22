package ru.radiationx.anilibria.presentation.release.details

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import moxy.InjectViewState
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleasePagerState
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
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
    private val commentsAnalytics: CommentsAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<ReleaseView>(router) {

    private var currentData: ReleaseItem? = null
    var releaseId = -1
    var releaseIdCode: String? = null
    var argReleaseItem: ReleaseItem? = null

    private val stateController = StateController(ReleasePagerState())

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        argReleaseItem?.also {
            updateLocalRelease(it)
        }
        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(ReleaseFull(it))
        }
        observeRelease()
        loadRelease()

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)
    }

    private fun loadRelease() {
        releaseInteractor
            .loadRelease(releaseId, releaseIdCode)
            .onStart { viewState.setRefreshing(true) }
            .onEach {
                viewState.setRefreshing(false)
                historyRepository.putRelease(it as ReleaseItem)
            }
            .launchIn(presenterScope)
    }

    private fun observeRelease() {
        releaseInteractor
            .observeFull(releaseId, releaseIdCode)
            .onEach { release ->
                updateLocalRelease(release)
                historyRepository.putRelease(release as ReleaseItem)

            }
            .launchIn(presenterScope)
    }

    private fun updateLocalRelease(release: ReleaseItem) {
        currentData = release
        releaseId = release.id
        releaseIdCode = release.code

        stateController.updateState {
            it.copy(
                poster = currentData?.poster,
                title = currentData?.let {
                    String.format("%s / %s", release.title, release.titleEng)
                }
            )
        }
    }

    fun onShareClick() {
        currentData?.let {
            releaseAnalytics.share(AnalyticsConstants.screen_release, it.id)
        }
        currentData?.link?.let {
            viewState.shareRelease(it)
        }
    }

    fun onCopyLinkClick() {
        currentData?.let {
            releaseAnalytics.copyLink(AnalyticsConstants.screen_release, it.id)
        }
        currentData?.link?.let {
            viewState.copyLink(it)
        }
    }

    fun onShortcutAddClick() {
        currentData?.let {
            releaseAnalytics.shortcut(AnalyticsConstants.screen_release, it.id)
            viewState.addShortCut(it)
        }
    }

    fun onCommentsSwipe() {
        currentData?.also {
            commentsAnalytics.open(AnalyticsConstants.screen_release, it.id)
        }
    }

}
