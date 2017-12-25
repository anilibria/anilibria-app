package ru.radiationx.anilibria.ui.fragments.release;

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.data.api.models.release.ReleaseFull
import ru.radiationx.anilibria.data.api.models.release.ReleaseItem
import ru.radiationx.anilibria.data.repository.ReleaseRepository
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter(private val releaseRepository: ReleaseRepository,
                       private val router: Router) : BasePresenter<ReleaseView>(router) {

    private var currentData: ReleaseFull? = null
    private var releaseId = -1

    fun setCurrentData(item: ReleaseItem) {
        viewState.showRelease(ReleaseFull(item))
    }

    fun setReleaseId(releaseId: Int) {
        this.releaseId = releaseId
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        if (releaseId > -1) {
            loadRelease()
        }
    }

    private fun loadRelease() {
        val disposable = releaseRepository.getRelease(releaseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ release ->
                    Log.d("SUKA", "subscribe call show")
                    viewState.setRefreshing(false)
                    viewState.showRelease(release)
                    currentData = release
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
    }

    fun onTorrentClick() {
        currentData?.torrentLink?.let {
            viewState.loadTorrent(it)
        }
    }

    fun onShareClick() {
        currentData?.link?.let {
            viewState.loadTorrent(it)
        }
    }

    fun onCopyLinkClick() {
        currentData?.link?.let {
            viewState.loadTorrent(it)
        }
    }

    fun onPlayAllClick() {
        currentData?.let {
            if (it.episodes.isEmpty()) {
                it.moonwalkLink?.let { viewState.playMoonwalk(it) }
            } else {
                viewState.playEpisodes(it)
            }
        }
    }

    fun onPlayEpisodeClick(position: Int, quality: Int) {
        currentData?.let {
            viewState.playEpisode(it, position, quality)
        }
    }

    fun openSearch(genre: String) {
        val args: Bundle = Bundle().apply {
            putString(SearchFragment.ARG_GENRE, genre)
        }
        router.navigateTo(Screens.RELEASES_SEARCH, args)
    }
}
