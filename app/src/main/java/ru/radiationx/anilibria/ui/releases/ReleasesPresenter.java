package ru.radiationx.anilibria.ui.releases;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.BasePresenter;

/**
 * Created by radiationx on 05.11.17.
 */

@InjectViewState
public class ReleasesPresenter extends BasePresenter<ReleaseView> {

    public ReleasesPresenter() {
        super();
        Log.e("SUKA", "NEW REL PRESENTER");
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        Log.e("SUKA", "onFirstViewAttach");
        getReleases(1);
    }

    void getReleases(int pageNum) {
        Log.e("SUKA", "getReleases");
        getViewState().setRefreshing(true);
        Disposable disposable =
                Api.get().Releases().getItems(pageNum)
                        .onErrorReturn(throwable -> new ArrayList<>())
                        .doOnError(throwable -> {
                            getViewState().setRefreshing(false);
                            Log.d("SUKA", "SAS");
                            throwable.printStackTrace();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(releaseItems -> {
                            getViewState().setRefreshing(false);
                            Log.d("SUKA", "subscribe call show");
                            getViewState().showReleases(releaseItems);
                        }, throwable -> {
                            getViewState().setRefreshing(false);
                            Log.d("SUKA", "SAS");
                            throwable.printStackTrace();
                        });
        addDisposable(disposable);
    }
}
