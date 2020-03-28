package ru.radiationx.anilibria.screen

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.common.fragment.GuidedAppScreen
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.auth.otp.AuthOtpGuidedFragment
import ru.radiationx.anilibria.screen.auth.credentials.AuthCredentialsGuidedFragment
import ru.radiationx.anilibria.screen.auth.main.AuthGuidedFragment
import ru.radiationx.anilibria.screen.config.ConfigFragment
import ru.radiationx.anilibria.screen.mainpages.MainPagesFragment
import ru.radiationx.anilibria.screen.details.DetailFragment
import ru.radiationx.anilibria.screen.player.PlayerActivity
import ru.radiationx.anilibria.screen.player.episodes.PlayerEpisodesGuidedFragment
import ru.radiationx.anilibria.screen.player.quality.PlayerQualityGuidedFragment
import ru.radiationx.anilibria.screen.player.speed.PlayerSpeedGuidedFragment
import ru.radiationx.anilibria.screen.trash.TestFlowFragment
import ru.radiationx.anilibria.screen.trash.TestFragment
import ru.radiationx.anilibria.screen.trash.VerticalGridTestFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

class ConfigScreen() : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return ConfigFragment()
    }
}

class MainPagesScreen() : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return MainPagesFragment()
    }
}

class GridScreen() : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return VerticalGridTestFragment()
    }
}

class DetailsScreen(private val releaseId: Int) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return DetailFragment.newInstance(releaseId)
    }
}


class FlowScreen() : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TestFlowFragment()
    }
}

class TestScreen() : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TestFragment()
    }
}

class AuthGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return AuthGuidedFragment()
    }
}

class AuthCredentialsGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return AuthCredentialsGuidedFragment()
    }
}

class AuthOtpGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return AuthOtpGuidedFragment()
    }
}

class PlayerScreen(val releaseId: Int, val episodeId: Int = -1) : SupportAppScreen() {
    override fun getActivityIntent(context: Context): Intent {
        return PlayerActivity.getIntent(context, releaseId, episodeId)
    }
}

class PlayerQualityGuidedScreen(val releaseId: Int = -1, val episodeId: Int = -1) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return PlayerQualityGuidedFragment.newInstance(releaseId, episodeId)
    }
}

class PlayerSpeedGuidedScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return PlayerSpeedGuidedFragment()
    }
}

class PlayerEpisodesGuidedScreen(val releaseId: Int = -1, val episodeId: Int = -1) : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return PlayerEpisodesGuidedFragment.newInstance(releaseId, episodeId)
    }
}

class TestGuidedStepScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return DialogExampleFragment()
    }
}