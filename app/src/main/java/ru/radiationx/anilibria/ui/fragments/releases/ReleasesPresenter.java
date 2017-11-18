package ru.radiationx.anilibria.ui.fragments.releases;

import android.os.Bundle;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.Screens;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment;
import ru.radiationx.anilibria.utils.mvp.BasePresenter;

/**
 * Created by radiationx on 05.11.17.
 */

@InjectViewState
public class ReleasesPresenter extends BasePresenter<ReleasesView> {
    private final static int START_PAGE = 1;
    private int currentPage = START_PAGE;

    public ReleasesPresenter() {
        super();
        Log.e("SUKA", "NEW REL PRESENTER");
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        Log.e("SUKA", "onFirstViewAttach");
        refreshReleases();
    }

    private boolean isFirstPage() {
        return currentPage == START_PAGE;
    }

    private void loadReleases(int pageNum) {
        Log.e("SUKA", "loadReleases");
        currentPage = pageNum;
        if (isFirstPage()) {
            getViewState().setRefreshing(true);
        }
        Disposable disposable = Api.get().Releases().getItems(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(releaseItems -> {
                    Log.d("SUKA", "subscribe call show");
                    if (isFirstPage()) {
                        getViewState().setRefreshing(false);
                        getViewState().showReleases(releaseItems);
                    } else {
                        getViewState().insertMore(releaseItems);
                    }
                }, throwable -> {
                    getViewState().setRefreshing(false);
                    Log.d("SUKA", "SAS");
                    throwable.printStackTrace();
                });
        addDisposable(disposable);
    }

    void refreshReleases() {
        loadReleases(START_PAGE);
    }

    void loadMore() {
        loadReleases(currentPage + 1);
    }

    void onItemClick(ReleaseItem item) {
        Bundle args = new Bundle();
        args.putInt(ReleaseFragment.ARG_ID, item.getId());
        App.get().getRouter().navigateTo(Screens.RELEASE_DETAILS, args);
    }

    boolean onItemLongClick(ReleaseItem item) {
        return false;
    }
}
