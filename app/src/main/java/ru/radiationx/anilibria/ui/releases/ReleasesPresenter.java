package ru.radiationx.anilibria.ui.releases;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.api.releases.ReleaseParser;
import ru.radiationx.anilibria.mvp.BasePresenter;

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
        Observable.fromCallable(() -> ReleaseParser.releaseItemsSync(pageNum))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(releaseItems -> {
                    view.setRefreshing(false);
                    view.showReleases(releaseItems);
                });
    }
}
