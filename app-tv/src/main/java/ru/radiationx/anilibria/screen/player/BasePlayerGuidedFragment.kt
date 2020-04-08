package ru.radiationx.anilibria.screen.player

import ru.radiationx.anilibria.common.fragment.scoped.ScopedGuidedStepFragment
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment.Companion.ARG_EPISODE_ID
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment.Companion.ARG_RELEASE_ID
import ru.radiationx.anilibria.screen.player.quality.PlayerQualityGuidedFragment
import ru.radiationx.shared.ktx.android.putExtra

abstract class BasePlayerGuidedFragment : ScopedGuidedStepFragment() {

    companion object {
        const val ARG_RELEASE_ID = "release id"
        const val ARG_EPISODE_ID = "episode id"
    }

    protected val releaseId by lazy { arguments?.getInt(ARG_RELEASE_ID) }
    protected val episodeId by lazy { arguments?.getInt(ARG_EPISODE_ID) }
}

fun <T : BasePlayerGuidedFragment> T.putIds(releaseId: Int = -1, episodeId: Int = -1): T = putExtra {
    putInt(ARG_RELEASE_ID, releaseId)
    putInt(ARG_EPISODE_ID, episodeId)
}