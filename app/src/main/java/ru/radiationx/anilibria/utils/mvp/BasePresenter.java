package ru.radiationx.anilibria.utils.mvp;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 05.11.17.
 */


public class BasePresenter<ViewT extends MvpView> extends MvpPresenter<ViewT> {
    private CompositeDisposable disposables = new CompositeDisposable();

    public BasePresenter() {
        super();
    }

    @Override
    public void onDestroy() {
        if (!disposables.isDisposed())
            disposables.dispose();
    }

    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }
}
