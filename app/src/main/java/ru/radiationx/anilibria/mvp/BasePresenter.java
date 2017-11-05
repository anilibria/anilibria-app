package ru.radiationx.anilibria.mvp;

/**
 * Created by radiationx on 05.11.17.
 */

public class BasePresenter<ViewT> implements IBasePresenter<ViewT> {
    protected ViewT view;

    public BasePresenter(ViewT view) {
        this.view = view;
    }

    @Override
    public void onResume(ViewT view) {
        this.view = view;
    }

    @Override
    public void onPause() {
        this.view = null;
    }
}
