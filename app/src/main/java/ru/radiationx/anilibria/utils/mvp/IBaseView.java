package ru.radiationx.anilibria.utils.mvp;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

/**
 * Created by radiationx on 05.11.17.
 */

public interface IBaseView extends MvpView {
    @StateStrategyType(SkipStrategy.class)
    void setRefreshing(boolean refreshing);
}
