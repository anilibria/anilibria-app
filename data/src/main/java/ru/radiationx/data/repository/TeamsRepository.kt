package ru.radiationx.data.repository

import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.TeamsApi
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.entity.mapper.toDomain
import toothpick.InjectConstructor

@InjectConstructor
class TeamsRepository(
    private val teamsApi: TeamsApi,
    private val schedulers: SchedulersProvider
) {

    fun getTeams(): Single<Teams> = teamsApi
        .getTeams()
        .map { it.toDomain() }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())
}