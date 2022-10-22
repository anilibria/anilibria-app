package ru.radiationx.anilibria.presentation.teams

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.TeamsAnalytics
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

    private val currentDataRelay = MutableStateFlow<Teams?>(null)

    private val queryRelay = MutableStateFlow(Query())

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        presenterScope.launch {
            runCatching {
                repository.requestUpdate()
            }.onFailure {
                it.printStackTrace()
            }
        }
        repository
            .observeTeams()
            .onStart { viewState.setLoading(true) }
            .onEach {
                viewState.setLoading(false)
                currentDataRelay.value = it
            }
            .launchIn(presenterScope)

        currentDataRelay
            .filterNotNull()
            .map { it.toState() }
            .flatMapLatest { teamStates ->
                queryRelay.map { query ->
                    teamStates.filterBy(query)
                }
            }
            .onEach {
                viewState.showData(it)
            }
            .launchIn(presenterScope)
    }

    fun setQueryText(text: String) {
        queryRelay.update { it.copy(text = text) }
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