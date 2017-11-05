package ru.radiationx.anilibria.mvp;

/**
 * Created by radiationx on 05.11.17.
 */

public interface IBasePresenter<ViewT> {
    void onResume(ViewT view);

    void onPause();
}
