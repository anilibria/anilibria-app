package ru.radiationx.anilibria.ui.fragments.release;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.BasePresenter;

/**
 * Created by radiationx on 18.11.17.
 */
@InjectViewState
public class ReleasePresenter extends BasePresenter<ReleaseView> {
    private ReleaseItem currentData = null;

    public void setCurrentData(ReleaseItem item) {
        currentData = item;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        if (currentData != null) {
            getViewState().showRelease(currentData);
        }
    }

    void loadRelease(int id) {
        Disposable disposable = Api.get().Release().getRelease(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(release -> {
                    Log.d("SUKA", "subscribe call show");
                    getViewState().setRefreshing(false);
                    getViewState().showRelease(release);
                    currentData = release;
                }, throwable -> {
                    getViewState().setRefreshing(false);
                    Log.d("SUKA", "SAS");
                    throwable.printStackTrace();
                });
        addDisposable(disposable);
    }

    void onTorrentClick() {
        if (currentData != null) {
            getViewState().loadTorrent(currentData.getTorrentLink());
        }
    }

    void onShareClick() {
        if (currentData != null) {
            getViewState().loadTorrent(currentData.getLink());
        }
    }

    void onCopyLinkClick() {
        if (currentData != null) {
            getViewState().loadTorrent(currentData.getLink());
        }
    }

}
