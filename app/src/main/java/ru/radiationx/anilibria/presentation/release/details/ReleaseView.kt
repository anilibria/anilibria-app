package ru.radiationx.anilibria.presentation.release.details

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.page.VkComments
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.TorrentItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/* Created by radiationx on 18.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseView : IBaseView {

    fun showRelease(release: ReleaseFull)

    @StateStrategyType(SkipStrategy::class)
    fun shareRelease(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun copyLink(url: String)

    @StateStrategyType(SkipStrategy::class)
    fun addShortCut(release: ReleaseItem)
}
