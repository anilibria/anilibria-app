package ru.radiationx.anilibria.ui.releases;

import java.util.ArrayList;

import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.IBasePresenter;
import ru.radiationx.anilibria.utils.mvp.IBaseView;

/**
 * Created by radiationx on 05.11.17.
 */

public interface ReleasesContract {
    interface View extends IBaseView {
        void showReleases(ArrayList<ReleaseItem> releases);
    }

    interface Presenter extends IBasePresenter<View> {
        void getReleases(int pageNum);
    }
}
