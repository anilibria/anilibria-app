package ru.radiationx.anilibria.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.radiationx.shared.ktx.addTo

open class LifecycleViewModel : ViewModel(), LifecycleObserver {

    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected open fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected open fun onDestroy() {
    }

    protected fun Disposable.untilDestroy() = this.addTo(disposables)

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    protected fun <T> Observable<T>.lifeSubscribe(
        action: (T) -> Unit
    ): Disposable = subscribe { action.invoke(it) }.untilDestroy()

    protected fun <T> Observable<T>.lifeSubscribe(
        action: (T) -> Unit,
        errorAction: ((Throwable) -> Unit)
    ): Disposable = subscribe({ action.invoke(it) }, { errorAction.invoke(it) }).untilDestroy()

    protected fun <T> Flowable<T>.lifeSubscribe(
        action: (T) -> Unit
    ): Disposable = subscribe { action.invoke(it) }.untilDestroy()

    protected fun <T> Flowable<T>.lifeSubscribe(
        action: (T) -> Unit,
        errorAction: ((Throwable) -> Unit)
    ): Disposable = subscribe({ action.invoke(it) }, { errorAction.invoke(it) }).untilDestroy()

    protected fun <T> Single<T>.lifeSubscribe(
        action: (T) -> Unit,
        errorAction: ((Throwable) -> Unit)
    ): Disposable = subscribe({ action.invoke(it) }, { errorAction.invoke(it) }).untilDestroy()

    protected fun <T> Maybe<T>.lifeSubscribe(
        action: (T) -> Unit,
        errorAction: ((Throwable) -> Unit)
    ): Disposable = subscribe({ action.invoke(it) }, { errorAction.invoke(it) }).untilDestroy()

    protected fun Completable.lifeSubscribe(
        action: () -> Unit,
        errorAction: ((Throwable) -> Unit)
    ): Disposable = subscribe({ action.invoke() }, { errorAction.invoke(it) }).untilDestroy()
}