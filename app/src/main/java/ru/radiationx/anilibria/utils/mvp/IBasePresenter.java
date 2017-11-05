package ru.radiationx.anilibria.utils.mvp;

/**
 * Created by radiationx on 05.11.17.
 */

public interface IBasePresenter<ViewT> {
    void onCreate(ViewT view);

    void onDestroy();
}
