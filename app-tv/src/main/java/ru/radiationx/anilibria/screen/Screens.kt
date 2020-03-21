package ru.radiationx.anilibria.screen

import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.common.fragment.DialogAppScreen
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.config.ConfigFragment
import ru.radiationx.anilibria.screen.mainpages.MainPagesFragment
import ru.radiationx.anilibria.screen.trash.TestDetailFragment
import ru.radiationx.anilibria.screen.trash.TestFlowFragment
import ru.radiationx.anilibria.screen.trash.TestFragment
import ru.radiationx.anilibria.screen.trash.VerticalGridTestFragment
import ru.radiationx.shared.ktx.android.putExtra
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

class DetailsScreen(private val id: Int) : SupportAppScreen() {
    override fun getFragment(): Fragment {
        return TestDetailFragment().putExtra {
            putInt("id", id)
        }
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