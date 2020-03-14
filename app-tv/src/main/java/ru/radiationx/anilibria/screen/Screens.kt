package ru.radiationx.anilibria.screen

import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.common.fragment.DialogAppScreen
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.config.ConfigFragment
import ru.radiationx.anilibria.screen.trash.TestFlowFragment
import ru.radiationx.anilibria.screen.trash.TestFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

class ConfigScreen() : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return ConfigFragment()
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

class TestGuidedStepScreen : DialogAppScreen() {
    override fun getFragment(): FakeGuidedStepFragment? {
        return DialogExampleFragment()
    }
}