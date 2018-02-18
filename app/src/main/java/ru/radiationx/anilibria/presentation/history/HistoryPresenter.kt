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

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        observeReleases()
    }

    private fun observeReleases() {
        historyRepository
                .observeReleases()
                .subscribe {
                    viewState.showReleases(it)
                }
                .addToDisposable()
    }

    fun onItemClick(item: ReleaseItem) {
        val args = Bundle()
        args.putInt(ReleaseFragment.ARG_ID, item.id)
        args.putString(ReleaseFragment.ARG_ID_CODE, item.idName)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        router.navigateTo(Screens.RELEASE_DETAILS, args)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}