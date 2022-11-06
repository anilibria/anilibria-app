package ru.radiationx.anilibria.screen.player

import ru.radiationx.anilibria.common.fragment.scoped.ScopedGuidedStepFragment
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment.Companion.ARG_EPISODE_ID
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment.Companion.ARG_RELEASE_ID
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.shared.ktx.android.putExtra

abstract class BasePlayerGuidedFragment : ScopedGuidedStepFragment() {

    companion object {
        const val ARG_RELEASE_ID = "release id"
        const val ARG_EPISODE_ID = "episode id"
    }

    protected val releaseId: ReleaseId by lazy {
        requireNotNull(requireArguments().getParcelable(ARG_RELEASE_ID))
    }
    protected val episodeId: EpisodeId? by lazy {
        requireArguments().getParcelable(ARG_EPISODE_ID)
    }
}

fun <T : BasePlayerGuidedFragment> T.putIds(
    releaseId: ReleaseId,
    episodeId: EpisodeId?
): T = putExtra {
    putParcelable(ARG_RELEASE_ID, releaseId)
    putParcelable(ARG_EPISODE_ID, episodeId)
}