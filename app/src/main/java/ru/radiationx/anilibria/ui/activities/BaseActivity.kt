package ru.radiationx.anilibria.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.arellomobile.mvp.MvpAppCompatActivity
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import javax.inject.Inject

@SuppressLint("Registered")
open class BaseActivity : MvpAppCompatActivity() {

    @Inject
    lateinit var screenMessagesObserver: ScreenMessagesObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        // injectDependencies()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
    }

}