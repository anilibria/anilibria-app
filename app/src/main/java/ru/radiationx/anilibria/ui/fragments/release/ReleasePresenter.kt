package ru.radiationx.anilibria.ui.fragments.release;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.BasePresenter;

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter : BasePresenter<ReleaseView>() {
    private var currentData: ReleaseItem? = null
    private var releaseId = -1

    fun setCurrentData(item: ReleaseItem) {
        currentData = item
    }

    fun setReleaseId(releaseId: Int) {
        this.releaseId = releaseId
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        currentData?.let {
            viewState.showRelease(it)
        }
        if (releaseId > -1) {
            loadRelease()
        }
    }

    private fun loadRelease() {
        val disposable = Api.get().Release().getRelease(releaseId)
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

    internal fun onTorrentClick() {
        currentData?.torrentLink?.let {
            viewState.loadTorrent(it)
        }
    }

    internal fun onShareClick() {
        currentData?.link?.let {
            viewState.loadTorrent(it)
        }
    }

    internal fun onCopyLinkClick() {
        currentData?.link?.let {
            viewState.loadTorrent(it)
        }
    }

    internal fun onPlayAllClick() {
        currentData?.let {
            if (it.episodes.isEmpty()) {
                it.moonwalkLink?.let { viewState.playMoonwalk(it) }
            } else {
                viewState.playEpisodes(it)
            }
        }
    }

    internal fun onPlayEpisodeClick(position: Int, quality: Int) {
        currentData?.let {
            viewState.playEpisode(it, position, quality)
        }
    }
}
