package ru.radiationx.anilibria.utils.mvp

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 05.11.17.
 */


open class BasePresenter<ViewT : MvpView>(private val router: Router) : MvpPresenter<ViewT>() {

    private var compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    fun Disposable.addToDisposable() {
        compositeDisposable.add(this)
    }

    fun onBackPressed() {
        router.exit()
    }
}
