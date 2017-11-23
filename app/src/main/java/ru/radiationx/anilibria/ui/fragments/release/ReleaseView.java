package ru.radiationx.anilibria.ui.fragments.release;

import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.IBaseView;

/**
 * Created by radiationx on 18.11.17.
 */

@StateStrategyType(AddToEndStrategy.class)
public interface ReleaseView extends IBaseView {
    void showRelease(ReleaseItem release);

    @StateStrategyType(SkipStrategy.class)
    void loadTorrent(String url);

    @StateStrategyType(SkipStrategy.class)
    void shareRelease(String text);

    @StateStrategyType(SkipStrategy.class)
    void copyLink(String url);
}
