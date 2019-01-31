package ru.radiationx.anilibria.presentation.release.details

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter(
        private val releaseRepository: ReleaseRepository,
        private val releaseInteractor: ReleaseInteractor,
        private val historyRepository: HistoryRepository,
        private val pageRepository: PageRepository,
        private val vitalRepository: VitalRepository,
        private val authRepository: AuthRepository,
        private val favoriteRepository: FavoriteRepository,
        private val router: Router,
        private val linkHandler: LinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<ReleaseView>(router) {

    var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    fun setCurrentData(item: ReleaseItem) {
        currentData = ReleaseFull(item)
        currentData?.let {
            viewState.showRelease(it)
        }
    }

    fun setLoadedData(data: ReleaseFull) {
        currentData = data
        currentData?.let {
            viewState.showRelease(it)
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadRelease()
    }

    private fun loadRelease() {
        Log.e("S_DEF_LOG", "load release $releaseId : $releaseIdCode : $currentData")
        releaseInteractor
                .observeRelease(releaseId, releaseIdCode)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({ release ->
                    releaseIdCode = release.code
                    Log.d("S_DEF_LOG", "subscribe call show")
                    viewState.showRelease(release)
                    viewState.setRefreshing(false)
                    currentData = release
                    historyRepository.putRelease(release as ReleaseItem)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    fun onShareClick() {
        Log.e("S_DEF_LOG", "onShareClick $currentData, ${currentData?.link}")
        currentData?.link?.let {
            Log.e("S_DEF_LOG", "onShareClick $it")
            viewState.shareRelease(it)
        }
    }

    fun onCopyLinkClick() {
        Log.e("S_DEF_LOG", "onShareClick $currentData, ${currentData?.link}")
        currentData?.link?.let {
            Log.e("S_DEF_LOG", "onShareClick $it")
            viewState.copyLink(it)
        }
    }

    fun onShortcutAddClick() {
        currentData?.let {
            viewState.addShortCut(it)
        }
    }

}
