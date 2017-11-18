package ru.radiationx.anilibria.ui.releases;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.ArrayList;

import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.IBaseView;

/**
 * Created by radiationx on 16.11.17.
 */
@StateStrategyType(AddToEndSingleStrategy.class)
public interface ReleasesView extends IBaseView {
    void showReleases(ArrayList<ReleaseItem> releases);

    void insertMore(ArrayList<ReleaseItem> releases);
}
