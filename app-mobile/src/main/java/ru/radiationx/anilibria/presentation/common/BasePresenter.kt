package ru.radiationx.anilibria.presentation.common

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.MvpPresenter
import moxy.MvpView
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 05.11.17.
 */


open class BasePresenter<ViewT : MvpView>(private val router: Router) : MvpPresenter<ViewT>() {

    private var compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    fun <T : Disposable> T.addToDisposable(): T {
        compositeDisposable.add(this)
        return this
    }

    fun onBackPressed() {
        router.exit()
    }
}
