package ru.radiationx.anilibria.screen

import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.common.fragment.GuidedAppScreen
import ru.radiationx.anilibria.screen.auth.credentials.AuthCredentialsGuidedFragment
import ru.radiationx.anilibria.screen.auth.main.AuthGuidedFragment
import ru.radiationx.anilibria.screen.auth.otp.AuthOtpGuidedFragment
import ru.radiationx.anilibria.screen.config.ConfigFragment
import ru.radiationx.anilibria.screen.details.DetailFragment
import ru.radiationx.anilibria.screen.details.other.DetailOtherGuidedFragment
import ru.radiationx.anilibria.screen.mainpages.MainPagesFragment
import ru.radiationx.anilibria.screen.player.PlayerFragment
import ru.radiationx.anilibria.screen.player.end_episode.EndEpisodeGuidedFragment
import ru.radiationx.anilibria.screen.player.end_season.EndSeasonGuidedFragment
import ru.radiationx.anilibria.screen.player.episodes.PlayerEpisodesGuidedFragment
import ru.radiationx.anilibria.screen.player.putIds
import ru.radiationx.anilibria.screen.player.quality.PlayerQualityGuidedFragment
import ru.radiationx.anilibria.screen.player.speed.PlayerSpeedGuidedFragment
import ru.radiationx.anilibria.screen.schedule.ScheduleFragment
import ru.radiationx.anilibria.screen.search.SearchFragment
import ru.radiationx.anilibria.screen.search.completed.SearchCompletedGuidedFragment
import ru.radiationx.anilibria.screen.search.genre.SearchGenreGuidedFragment
import ru.radiationx.anilibria.screen.search.putValues
import ru.radiationx.anilibria.screen.search.season.SearchSeasonGuidedFragment
import ru.radiationx.anilibria.screen.search.sort.SearchSortGuidedFragment
import ru.radiationx.anilibria.screen.search.year.SearchYearGuidedFragment
import ru.radiationx.anilibria.screen.suggestions.SuggestionsFragment
import ru.radiationx.anilibria.screen.trash.TestFragment
import ru.radiationx.anilibria.screen.update.UpdateFragment
import ru.radiationx.anilibria.screen.update.source.UpdateSourceGuidedFragment
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.terrakok.cicerone.android.support.SupportAppScreen

class ConfigScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return ConfigFragment()
    }
}

class MainPagesScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return MainPagesFragment()
    }
}

class DetailsScreen(private val releaseId: ReleaseId) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return DetailFragment.newInstance(releaseId)
    }
}

class DetailOtherGuidedScreen(private val releaseId: ReleaseId) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return DetailOtherGuidedFragment.newInstance(releaseId)
    }
}

class ScheduleScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return ScheduleFragment()
    }
}

class UpdateScreen
    : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return UpdateFragment()
    }
}

class UpdateSourceScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return UpdateSourceGuidedFragment()
    }
}

class SuggestionsScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return SuggestionsFragment()
    }
}

class SearchScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return SearchFragment()
    }
}

class SearchYearGuidedScreen(private val values: List<String>) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return SearchYearGuidedFragment().putValues(values)
    }
}

class SearchSeasonGuidedScreen(private val values: List<String>) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return SearchSeasonGuidedFragment().putValues(values)
    }
}

class SearchGenreGuidedScreen(private val values: List<String>) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return SearchGenreGuidedFragment().putValues(values)
    }
}

class SearchSortGuidedScreen(private val sort: SearchForm.Sort) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return SearchSortGuidedFragment.newInstance(sort)
    }
}

class SearchCompletedGuidedScreen(private val onlyCompleted: Boolean) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return SearchCompletedGuidedFragment.newInstance(onlyCompleted)
    }
}

class TestScreen : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TestFragment()
    }
}

class AuthGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return AuthGuidedFragment()
    }
}

class AuthCredentialsGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return AuthCredentialsGuidedFragment()
    }
}

class AuthOtpGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return AuthOtpGuidedFragment()
    }
}

class PlayerScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return PlayerFragment.newInstance(releaseId, episodeId)
    }
}

class PlayerQualityGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return PlayerQualityGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerSpeedGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return PlayerSpeedGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerEpisodesGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return PlayerEpisodesGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerEndEpisodeGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return EndEpisodeGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerEndSeasonGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return EndSeasonGuidedFragment().putIds(releaseId, episodeId)
    }
}

class TestGuidedStepScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment {
        return DialogExampleFragment()
    }
}