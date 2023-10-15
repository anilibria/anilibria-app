package ru.radiationx.anilibria.ui.fragments.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.features.TeamsAnalytics
import ru.radiationx.data.entity.domain.team.Team
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.repository.TeamsRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.InjectConstructor

@OptIn(ExperimentalCoroutinesApi::class)
@InjectConstructor
class TeamsViewModel(
    private val router: Router,
    private val repository: TeamsRepository,
    private val systemUtils: SystemUtils,
    private val analytics: TeamsAnalytics
) : ViewModel() {

    private val currentDataRelay = MutableStateFlow<Teams?>(null)

    private val queryRelay = MutableStateFlow(Query())

    private val _state = MutableStateFlow(TeamsScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            coRunCatching {
                repository.requestUpdate()
            }.onFailure {
                Timber.e(it)
            }
        }
        repository
            .observeTeams()
            .onStart {
                _state.update { it.copy(loading = true) }
            }
            .onEach { data ->
                _state.update { it.copy(loading = false) }
                currentDataRelay.value = data
            }
            .launchIn(viewModelScope)

        currentDataRelay
            .filterNotNull()
            .map { it.toState() }
            .flatMapLatest { teamStates ->
                queryRelay.map { query ->
                    teamStates.filterBy(query)
                }
            }
            .onEach { data ->
                _state.update { it.copy(data = data) }
            }
            .launchIn(viewModelScope)
    }

    fun onBackPressed() {
        router.exit()
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

data class TeamsScreenState(
    val data: TeamsState? = null,
    val loading: Boolean = false
)