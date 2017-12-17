package ru.radiationx.anilibria.ui.activities.main

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 17.12.17.
 */
@InjectViewState
class MainPresenter(private val router: Router) : BasePresenter<MainView>(router) {

    fun selectTab(screenKey: String) {
        Log.e("SUKA", "presenter selectTab "+screenKey)
        viewState.highlightTab(screenKey)
        router.replaceScreen(screenKey)
    }

}