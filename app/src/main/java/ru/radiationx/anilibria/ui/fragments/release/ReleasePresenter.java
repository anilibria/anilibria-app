package ru.radiationx.anilibria.ui.fragments.release;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.utils.mvp.BasePresenter;

/**
 * Created by radiationx on 18.11.17.
 */
@InjectViewState
public class ReleasePresenter extends BasePresenter<ReleaseView> {

    void loadRelease(int id) {
        Disposable disposable = Api.get().Release().getRelease(id)
                .map(release -> {
                    String original = release.getOriginalTitle();
                    String seasonsHtml = "<b>Сезон:</b> " + TextUtils.join(", ", release.getSeasons());
                    String voicesHtml = "<b>Голоса:</b> " + TextUtils.join(", ", release.getVoices());
                    String typesHtml = "<b>Тип:</b> " + TextUtils.join(", ", release.getTypes());
                    String[] arrHtml = {original, seasonsHtml, voicesHtml, typesHtml};
                    String html = TextUtils.join("<br>", arrHtml);
                    return new Pair<>(release, html);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Log.d("SUKA", "subscribe call show");
                    getViewState().setRefreshing(false);
                    getViewState().showRelease(pair.first, pair.second);
                }, throwable -> {
                    getViewState().setRefreshing(false);
                    Log.d("SUKA", "SAS");
                    throwable.printStackTrace();
                });
        addDisposable(disposable);
    }
}
