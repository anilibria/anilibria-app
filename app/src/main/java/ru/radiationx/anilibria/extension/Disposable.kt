package ru.radiationx.anilibria.extension

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(disposables: CompositeDisposable): Disposable {
    disposables.add(this)
    return this
}