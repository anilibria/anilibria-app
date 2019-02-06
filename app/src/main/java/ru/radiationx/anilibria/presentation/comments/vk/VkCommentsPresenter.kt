package ru.radiationx.anilibria.presentation.comments.vk

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.page.VkComments
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.presentation.comments.CommentsView
import ru.radiationx.anilibria.ui.activities.auth.AuthActivity
import ru.radiationx.anilibria.ui.fragments.auth.AuthVkFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.radiationx.anilibria.ui.navigation.AppRouter

@InjectViewState
class VkCommentsPresenter(
        private val pageRepository: PageRepository,
        private val releaseInteractor: ReleaseInteractor,
        private val authHolder: AuthHolder,
        private val router: AppRouter,
        private val linkHandler: LinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<VkCommentsView>(router) {

    var currentData: ReleaseFull? = null
    var currentVkComments: VkComments? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        val releaseItem = releaseInteractor.getItem(releaseId, releaseIdCode)
        if (releaseItem == null) {
            loadRelease()
        } else {
            currentData = ReleaseFull(releaseItem)
        }
        loadData()
        authHolder.observeVkAuthChange()
                .subscribe {
                    updateComments()
                }
                .addToDisposable()
    }

    fun authRequest(url: String) {
        router.navigateTo(Screens.Auth(Screens.AuthVk(url)))
    }

    fun loadData() {
        pageRepository
                .getComments()
                .subscribe({
                    currentVkComments = it
                    updateComments()
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun loadRelease() {
        releaseInteractor
                .loadRelease(releaseId, releaseIdCode)
                .subscribe({ release ->
                    currentData = release
                    updateComments()
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    fun updateComments() {
        if (currentData != null && currentVkComments != null) {
            currentVkComments?.also {
                viewState.showBody(VkComments(
                        "${it.baseUrl}release/${currentData?.code}.html",
                        it.script
                ))
            }
        }
    }
}