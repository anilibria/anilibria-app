package ru.radiationx.data

import io.reactivex.Scheduler

@Deprecated("used in rx, but now useless")
interface SchedulersProvider {
    fun ui(): Scheduler
    fun computation(): Scheduler
    fun trampoline(): Scheduler
    fun newThread(): Scheduler
    fun io(): Scheduler

}
