package ru.radiationx.anilibria.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.arellomobile.mvp.MvpAppCompatActivity
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver

@SuppressLint("Registered")
open class BaseActivity : MvpAppCompatActivity() {

    private val screenMessagesObserver = ScreenMessagesObserver()
    protected val screenMessenger: SystemMessenger
        get() = screenMessagesObserver.screenMessenger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
        screenMessagesObserver.context = this
    }

}