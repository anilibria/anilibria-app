package ru.radiationx.anilibria.presentation.common;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

/**
 * Created by radiationx on 05.11.17.
 */
public interface IBaseView extends MvpView {
    @StateStrategyType(AddToEndSingleStrategy.class)
    void setRefreshing(boolean refreshing);
}
