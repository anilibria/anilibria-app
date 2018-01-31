package ru.radiationx.anilibria.presentation.release.list

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/* Created by radiationx on 16.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleasesView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showReleases(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(releases: List<ReleaseItem>)

    fun setEndless(enable: Boolean)

    fun showVitalBottom(vital: VitalItem)

    fun showVitalItems(vital: List<VitalItem>)
}
