package ru.radiationx.anilibria.utils;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 05.11.17.
 */

public class RxBase {

    public static <T> Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, Consumer<Throwable> onError, @NonNull T onErrorReturn) {
        return observable
                .onErrorReturn(throwable -> onErrorReturn)
                .doOnError(onError)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError);
    }

}
