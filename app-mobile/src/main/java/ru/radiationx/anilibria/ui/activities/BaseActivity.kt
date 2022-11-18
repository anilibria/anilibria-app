package ru.radiationx.anilibria.ui.activities

import android.os.Bundle
import androidx.annotation.LayoutRes
import moxy.MvpAppCompatActivity
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import javax.inject.Inject

open class BaseActivity(
    @LayoutRes contentLayoutId: Int = 0
) : MvpAppCompatActivity(contentLayoutId) {

    @Inject
    lateinit var screenMessagesObserver: ScreenMessagesObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        // injectDependencies()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
    }
}