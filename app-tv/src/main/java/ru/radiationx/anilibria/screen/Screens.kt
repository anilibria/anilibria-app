package ru.radiationx.anilibria.screen

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.leanback.app.GuidedStepSupportFragment
import ru.radiationx.anilibria.common.fragment.DialogAppScreen
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
    override fun getFragment(): GuidedStepSupportFragment? {
        return DialogExampleFragment()
    }
}