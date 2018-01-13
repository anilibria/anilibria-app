package ru.radiationx.anilibria.presentation.release.details;

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter(
        private val releaseRepository: ReleaseRepository,
        private val router: Router
) : BasePresenter<ReleaseView>(router) {

    private var currentData: ReleaseFull? = null
    private var releaseId = -1
    private var releaseIdName: String? = null

    fun setCurrentData(item: ReleaseItem) {
        viewState.showRelease(ReleaseFull(item))
    }

    fun setReleaseId(releaseId: Int) {
        this.releaseId = releaseId
    }

    fun setReleaseIdName(releaseIdName: String) {
        this.releaseIdName = releaseIdName
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        loadRelease()
    }

    private fun loadRelease() {
        val source = when {
            releaseId != -1 -> releaseRepository.getRelease(releaseId)
            releaseIdName != null -> releaseRepository.getRelease(releaseIdName!!)
            else -> return
        }
        source.subscribeOn(Schedulers.io())
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
                .addToDisposable()
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

    fun onPlayEpisodeClick(episode: ReleaseFull.Episode, quality: Int) {
        currentData?.let {
            viewState.playEpisode(it, it.episodes.indexOf(episode), quality)
        }
    }

    fun openSearch(genre: String) {
        val args: Bundle = Bundle().apply {
            putString(SearchFragment.ARG_GENRE, genre)
        }
        router.navigateTo(Screens.RELEASES_SEARCH, args)
    }
}
