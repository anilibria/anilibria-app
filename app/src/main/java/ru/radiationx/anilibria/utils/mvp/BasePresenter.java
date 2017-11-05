package ru.radiationx.anilibria.utils.mvp;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 05.11.17.
 */

public class BasePresenter<ViewT> implements IBasePresenter<ViewT> {
    protected ViewT view;
    private CompositeDisposable disposables = new CompositeDisposable();

    public BasePresenter(ViewT view) {
        this.view = view;
    }

    @Override
    public void onCreate(ViewT view) {
        this.view = view;
    }

    @Override
    public void onDestroy() {
        if (!disposables.isDisposed())
            disposables.dispose();
        this.view = null;
    }

    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }
}
