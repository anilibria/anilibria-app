package ru.radiationx.anilibria.ui.fragments.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.features.TeamsAnalytics
import ru.radiationx.data.api.teams.TeamsRepository
import ru.radiationx.data.api.teams.models.Team
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class TeamsViewModel @Inject constructor(
    private val router: Router,
    private val repository: TeamsRepository,
    private val systemUtils: SystemUtils,
    private val analytics: TeamsAnalytics
) : ViewModel() {

    private val currentDataRelay = MutableStateFlow<List<Team>?>(null)

    private val queryRelay = MutableStateFlow(Query())

    private val _state = MutableStateFlow(TeamsScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            coRunCatching {
                repository.getTeams()
            }.onSuccess {
                currentDataRelay.value = it
            }.onFailure {
                Timber.e(it)
            }
            _state.update { it.copy(loading = false) }
        }

        currentDataRelay
            .filterNotNull()
            .map { it.toTeamsState() }
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
        systemUtils.open("https://t.me/joinlibria_bot")
    }

    private fun List<Team>.toTeamsState(): TeamsState = TeamsState(
        hasQuery = false,
        teams = this.toState()
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
                nickname = user.nickname,
                roles = user.roles.map { it.title },
                tags = tags
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