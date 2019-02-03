package ru.radiationx.anilibria.presentation.history

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.repository.HistoryRepository
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 18.02.18.
 */
@InjectViewState
class HistoryPresenter(
        private val router: Router,
        private val historyRepository: HistoryRepository
) : BasePresenter<HistoryView>(router) {

    private val currentReleases = mutableListOf<ReleaseItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        observeReleases()
    }

    private fun observeReleases() {
        historyRepository
                .observeReleases()
                .subscribe {
                    currentReleases.clear()
                    currentReleases.addAll(it)
                    viewState.showReleases(it)
                }
                .addToDisposable()
    }

    fun localSearch(query: String) {
        if (!query.isEmpty()) {
            val searchRes = currentReleases.filter {
                it.title.orEmpty().contains(query, true) || it.titleEng.orEmpty().contains(query, true)
            }
            viewState.showReleases(searchRes)
        } else {
            viewState.showReleases(currentReleases)
        }
    }

    fun onItemClick(item: ReleaseItem) {
        val args = Bundle()
        args.putInt(ReleaseFragment.ARG_ID, item.id)
        args.putString(ReleaseFragment.ARG_ID_CODE, item.code)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        router.navigateTo(Screens.RELEASE_DETAILS, args)
    }

    fun onDeleteClick(item: ReleaseItem) {
        historyRepository.removeRelease(item.id)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}