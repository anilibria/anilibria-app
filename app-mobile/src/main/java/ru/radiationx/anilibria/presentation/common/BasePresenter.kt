package ru.radiationx.anilibria.presentation.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import moxy.MvpPresenter
import moxy.MvpView
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 05.11.17.
 */


open class BasePresenter<ViewT : MvpView>(private val router: Router) : MvpPresenter<ViewT>() {

    val presenterScope by lazy {
        CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    override fun onDestroy() {
        presenterScope.cancel()
    }

    fun onBackPressed() {
        router.exit()
    }
}
