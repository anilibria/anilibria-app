package ru.radiationx.anilibria.ui.fragments.releases;

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.ArrayList;

import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.utils.mvp.IBaseView;

/* Created by radiationx on 16.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleasesView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showReleases(releases: ArrayList<ReleaseItem>);

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(releases: ArrayList<ReleaseItem>);

    fun setEndless(enable: Boolean)
}
