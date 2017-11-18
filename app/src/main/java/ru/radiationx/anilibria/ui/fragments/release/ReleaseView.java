package ru.radiationx.anilibria.ui.fragments.release;

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import ru.radiationx.anilibria.data.api.release.FullRelease;
import ru.radiationx.anilibria.utils.mvp.IBaseView;

/**
 * Created by radiationx on 18.11.17.
 */

@StateStrategyType(AddToEndStrategy.class)
public interface ReleaseView extends IBaseView {
    void showRelease(FullRelease release);
    void showRelease(FullRelease release, String infoText);
}
