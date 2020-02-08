package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable
import ru.radiationx.data.entity.app.other.LinkMenuItem

interface MenuHolder {
    fun observe(): Observable<List<LinkMenuItem>>
    fun save(items: List<LinkMenuItem>)
    fun get(): List<LinkMenuItem>
}