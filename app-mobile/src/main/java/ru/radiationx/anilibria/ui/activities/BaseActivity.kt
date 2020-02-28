package ru.radiationx.anilibria.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import moxy.MvpAppCompatActivity
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import ru.radiationx.shared_app.DI
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


    fun <T, A : FragmentActivity> A.getDependency(clazz: Class<T>, scope: String): T = DI.get(clazz, DI.DEFAULT_SCOPE, scope)

}