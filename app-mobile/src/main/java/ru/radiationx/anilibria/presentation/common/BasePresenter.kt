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


@Deprecated("use viewmodel", level = DeprecationLevel.ERROR)
open class BasePresenter<ViewT : MvpView>(private val router: Router) : MvpPresenter<ViewT>() {

    val viewModelScope by lazy {
        CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun onBackPressed() {
        router.exit()
    }
}
