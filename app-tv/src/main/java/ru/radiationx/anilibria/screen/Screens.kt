package ru.radiationx.anilibria.screen

import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.common.fragment.GuidedAppScreen
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.auth.otp.AuthOtpGuidedFragment
import ru.radiationx.anilibria.screen.auth.credentials.AuthCredentialsGuidedFragment
import ru.radiationx.anilibria.screen.auth.main.AuthGuidedFragment
import ru.radiationx.anilibria.screen.config.ConfigFragment
import ru.radiationx.anilibria.screen.mainpages.MainPagesFragment
import ru.radiationx.anilibria.screen.details.DetailFragment
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

class TestGuidedStepScreen : GuidedAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return DialogExampleFragment()
    }
}