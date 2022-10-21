package ru.radiationx.data.repository

import io.reactivex.Completable
import io.reactivex.Observable
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.TeamsHolder
import ru.radiationx.data.datasource.remote.api.TeamsApi
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.entity.mapper.toDomain
import toothpick.InjectConstructor

@InjectConstructor
class TeamsRepository(
    private val teamsApi: TeamsApi,
    private val teamsHolder: TeamsHolder,
    private val schedulers: SchedulersProvider
) {

    fun requestUpdate(): Completable = teamsApi
        .getTeams()
        .flatMapCompletable { teamsHolder.save(it) }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun observeTeams(): Observable<Teams> = teamsHolder
        .observe()
        .map { it.toDomain() }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())
}