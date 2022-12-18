package ru.radiationx.anilibria.screen.player

import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment.Companion.ARG_EPISODE_ID
import ru.radiationx.anilibria.screen.player.BasePlayerGuidedFragment.Companion.ARG_RELEASE_ID
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra

data class PlayerExtra(
    val releaseId: ReleaseId,
    val episodeId: EpisodeId?
) : QuillExtra

abstract class BasePlayerGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        const val ARG_RELEASE_ID = "release id"
        const val ARG_EPISODE_ID = "episode id"
    }

    protected val argExtra by lazy {
        PlayerExtra(
            releaseId = getExtraNotNull(ARG_RELEASE_ID),
            episodeId = getExtra(ARG_EPISODE_ID)
        )
    }
}

fun <T : BasePlayerGuidedFragment> T.putIds(
    releaseId: ReleaseId,
    episodeId: EpisodeId?
): T = putExtra {
    putParcelable(ARG_RELEASE_ID, releaseId)
    putParcelable(ARG_EPISODE_ID, episodeId)
}