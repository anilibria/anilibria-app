package ru.radiationx.anilibria.presentation.checker

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.model.repository.CheckerRepository
import ru.radiationx.anilibria.presentation.ErrorHandler

/**
 * Created by radiationx on 28.01.18.
 */
@InjectViewState
class CheckerPresenter(
        private val checkerRepository: CheckerRepository,
        private val errorHandler: ErrorHandler
) : MvpPresenter<CheckerView>() {

    var forceLoad = false

    private var compositeDisposable = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        checkUpdate()
    }

    private fun checkUpdate() {
        Log.e("CHECKER", "checkUpdate presenter")
        checkerRepository
                .checkUpdate(BuildConfig.VERSION_CODE, forceLoad)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({
                    Log.e("CHECKER", "SUBSC DATE " + it)
                    viewState.setRefreshing(false)
                    viewState.showUpdateData(it)
                }, {
                    viewState.setRefreshing(false)
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    fun Disposable.addToDisposable() {
        compositeDisposable.add(this)
    }
}