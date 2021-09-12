package ru.radiationx.anilibria.presentation.comments

import android.util.Log
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthVkAnalytics
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.entity.app.page.VkComments
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.PageRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class VkCommentsPresenter @Inject constructor(
    private val userHolder: UserHolder,
    private val pageRepository: PageRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val authHolder: AuthHolder,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val authVkAnalytics: AuthVkAnalytics,
    private val commentsAnalytics: CommentsAnalytics
) : BasePresenter<VkCommentsView>(router) {

    private var currentData: ReleaseFull? = null
    private var currentVkComments: VkComments? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    private var isVisibleToUser = false
    private var pendingAuthRequest: String? = null
    private var authRequestDisposable = Disposables.disposed()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        val releaseItem = releaseInteractor.getItem(releaseId, releaseIdCode)
        if (releaseItem == null) {
            loadRelease()
        } else {
            currentData = ReleaseFull(releaseItem)
        }
        loadData()

        userHolder
            .observeUser()
            .map { it.authState }
            .distinctUntilChanged()
            .subscribe { updateComments() }
            .addToDisposable()

        authHolder.observeVkAuthChange()
            .subscribe { updateComments() }
            .addToDisposable()
    }

    fun setVisibleToUser(isVisible: Boolean) {
        isVisibleToUser = isVisible
        tryExecutePendingAuthRequest()
    }

    fun authRequest(url: String) {
        pendingAuthRequest = url
        tryExecutePendingAuthRequest()
    }

    fun onPageLoaded() {
        commentsAnalytics.loaded()
    }

    fun onPageCommitError(error: Exception) {
        commentsAnalytics.error(error)
    }

    private fun tryExecutePendingAuthRequest() {
        authRequestDisposable.dispose()
        authRequestDisposable = Completable
            .fromAction {
                val url = pendingAuthRequest
                if (isVisibleToUser && url != null) {
                    pendingAuthRequest = null
                    authVkAnalytics.open(AnalyticsConstants.screen_auth_vk)
                    router.navigateTo(Screens.Auth(Screens.AuthVk(url)))
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addToDisposable()
    }

    private fun loadData() {
        pageRepository
            .getComments()
            .subscribe({
                currentVkComments = it
                updateComments()
            }, {
                commentsAnalytics.error(it)
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    private fun loadRelease() {
        releaseInteractor
            .loadRelease(releaseId, releaseIdCode)
            .subscribe({ release ->
                currentData = release
                updateComments()
            }) {
                commentsAnalytics.error(it)
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    private fun updateComments() {
        if (currentData != null && currentVkComments != null) {
            currentVkComments?.also {
                viewState.showBody(
                    VkComments(
                        "${it.baseUrl}release/${currentData?.code}.html",
                        it.script
                    )
                )
            }
        }
    }
}