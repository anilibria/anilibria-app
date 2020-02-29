package ru.radiationx.anilibria

import android.os.Bundle
import android.view.View
import ru.radiationx.shared_app.FlowFragment

class TestFlowFragment : FlowFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        router.newRootScreen(TestScreen())

    }
}