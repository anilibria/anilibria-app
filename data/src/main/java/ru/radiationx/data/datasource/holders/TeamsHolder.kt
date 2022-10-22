package ru.radiationx.data.datasource.holders

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.team.TeamsResponse

interface TeamsHolder {
    fun observe(): Observable<TeamsResponse>
    fun get(): Single<TeamsResponse>
    fun save(data: TeamsResponse): Completable
    fun delete(): Completable
}