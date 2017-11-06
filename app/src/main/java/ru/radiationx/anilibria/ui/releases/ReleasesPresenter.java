package ru.radiationx.anilibria.ui.releases;

import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.utils.mvp.BasePresenter;

/**
 * Created by radiationx on 05.11.17.
 */

public class ReleasesPresenter extends BasePresenter<ReleasesContract.View> implements
        ReleasesContract.Presenter {

    ReleasesPresenter(ReleasesContract.View view) {
        super(view);
    }

    @Override
    public void getReleases(int pageNum) {
        view.setRefreshing(true);
        Disposable disposable =
                Api.get().Releases().getItems(pageNum)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(releaseItems -> {
                            view.setRefreshing(false);
                            view.showReleases(releaseItems);
                        }, throwable -> {
                            view.setRefreshing(false);
                            Log.d("SUKA", "SAS");
                            throwable.printStackTrace();
                        });
        addDisposable(disposable);
    }
}
