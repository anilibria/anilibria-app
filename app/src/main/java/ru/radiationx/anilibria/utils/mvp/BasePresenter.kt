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
    private val disposables = CompositeDisposable()

    override fun onDestroy() {
        if (!disposables.isDisposed)
            disposables.dispose()
    }

    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun onBackPressed() {
        router.exit()
    }
}
