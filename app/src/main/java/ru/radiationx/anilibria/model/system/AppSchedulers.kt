package ru.radiationx.anilibria.model.system

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppSchedulers : SchedulersProvider {
    override fun ui() = AndroidSchedulers.mainThread()
    override fun computation() = Schedulers.computation()
    override fun trampoline() = Schedulers.trampoline()
    override fun newThread() = Schedulers.newThread()
    override fun io() = Schedulers.io()
}

