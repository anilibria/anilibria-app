package ru.radiationx.anilibria.presentation.release.details

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleasePagerState
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter @Inject constructor(
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val commentsAnalytics: CommentsAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<ReleaseView>(router) {

    private var currentData: Release? = null
    var releaseId: ReleaseId? = null
    var releaseIdCode: ReleaseCode? = null
    var argReleaseItem: Release? = null

    private val stateController = StateController(ReleasePagerState())

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        argReleaseItem?.also {
            updateLocalRelease(it)
        }
        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(it)
        }
        observeRelease()
        loadRelease()
        subscribeAuth()

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)
    }

    private fun subscribeAuth() {
        authRepository
            .observeAuthState()
            .drop(1)
            .onEach { loadRelease() }
            .launchIn(presenterScope)
    }

    private fun loadRelease() {
        presenterScope.launch {
            viewState.setRefreshing(true)
            runCatching {
                releaseInteractor.loadRelease(releaseId, releaseIdCode)
            }.onSuccess {
                historyRepository.putRelease(it as Release)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun observeRelease() {
        releaseInteractor
            .observeFull(releaseId, releaseIdCode)
            .onEach { release ->
                updateLocalRelease(release)
                historyRepository.putRelease(release as Release)
            }
            .launchIn(presenterScope)
    }

    private fun updateLocalRelease(release: Release) {
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
            releaseAnalytics.share(AnalyticsConstants.screen_release, it.id.id)
        }
        currentData?.link?.let {
            viewState.shareRelease(it)
        }
    }

    fun onCopyLinkClick() {
        currentData?.let {
            releaseAnalytics.copyLink(AnalyticsConstants.screen_release, it.id.id)
        }
        currentData?.link?.let {
            viewState.copyLink(it)
        }
    }

    fun onShortcutAddClick() {
        currentData?.let {
            releaseAnalytics.shortcut(AnalyticsConstants.screen_release, it.id.id)
            viewState.addShortCut(it)
        }
    }

    fun onCommentsSwipe() {
        currentData?.also {
            commentsAnalytics.open(AnalyticsConstants.screen_release, it.id.id)
        }
    }

}
