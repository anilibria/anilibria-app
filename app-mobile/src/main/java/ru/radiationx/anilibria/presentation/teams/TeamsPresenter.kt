package ru.radiationx.anilibria.presentation.teams

import com.jakewharton.rxrelay2.BehaviorRelay
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.TeamsAnalytics
import ru.radiationx.data.entity.common.DataWrapper
import ru.radiationx.data.entity.domain.team.Team
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.repository.TeamsRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class TeamsPresenter(
    router: Router,
    private val repository: TeamsRepository,
    private val errorHandler: IErrorHandler,
    private val systemUtils: SystemUtils,
    private val analytics: TeamsAnalytics
) : BasePresenter<TeamsView>(router) {

    private val currentDataRelay =
        BehaviorRelay.createDefault<DataWrapper<Teams>>(DataWrapper(null))

    private val queryRelay = BehaviorRelay.createDefault(Query())

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        repository
            .requestUpdate()
            .subscribe({}, {
                it.printStackTrace()
            })
            .addToDisposable()
        repository
            .observeTeams()
            .doOnSubscribe { viewState.setLoading(true) }
            .subscribe({
                viewState.setLoading(false)
                currentDataRelay.accept(DataWrapper(it))
            }, {
                viewState.setLoading(false)
                errorHandler.handle(it)
            })
            .addToDisposable()

        currentDataRelay
            .filter { it.data != null }
            .map { it.data!! }
            .map { it.toState() }
            .flatMap { teamStates ->
                queryRelay.map { query ->
                    teamStates.filterBy(query)
                }
            }
            .subscribe {
                viewState.showData(it)
            }
            .addToDisposable()
    }

    fun setQueryText(text: String) {
        queryRelay.accept(queryRelay.value!!.copy(text = text))
    }

    fun onHeaderActionClick() {
        analytics.joinClick()
        systemUtils.externalLink("https://t.me/joinlibria_bot")
    }

    private fun Teams.toState(): TeamsState = TeamsState(
        false,
        headerRoles,
        teams.toState()
    )

    private fun List<Team>.toState(): List<TeamState> = map { team ->
        val section = TeamSectionState(team.title, team.description)
        val users = team.users.map { user ->
            val tags = mutableListOf<String>().apply {
                if (user.isIntern) {
                    add("Стажер")
                }
                if (user.isVacation) {
                    add("В отпуске")
                }
            }
            TeamUserState(
                user.nickname,
                user.roles.find { it.color != null }?.color,
                user.roles.map { it.title },
                tags
            )
        }
        TeamState(section, users)
    }

    private fun TeamsState.filterBy(query: Query): TeamsState {
        val newTeams = teams.map { teamState ->
            val newUsers = teamState.users
                .filter { user ->
                    query.text.isEmpty()
                            || user.nickname.contains(query.text, true)
                            || user.roles.any { it.contains(query.text, true) }
                            || user.tags.any { it.contains(query.text, true) }
                }
            teamState.copy(users = newUsers)
        }.filter {
            it.users.isNotEmpty()
        }
        return copy(teams = newTeams, hasQuery = query.text.isNotEmpty())
    }

    private data class Query(
        val text: String = ""
    )
}