package ru.radiationx.anilibria.ui.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen
import timber.log.Timber
import toothpick.InjectConstructor


data class MainScreenState(
    val selectedTab: String? = null,
    val needConfig: Boolean = false,
    val mainLogicCompleted: Boolean = false
)

@InjectConstructor
class MainViewModel(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val donationRepository: DonationRepository,
    private val apiConfig: ApiConfig,
    private val analyticsProfile: AnalyticsProfile,
    private val authMainAnalytics: AuthMainAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val favoritesAnalytics: FavoritesAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics,
    private val otherAnalytics: OtherAnalytics
) : ViewModel() {

    private val defaultScreen = Screens.MainFeed().screenKey!!

    private var firstLaunch = true

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    val updateTabsAction = EventFlow<Unit>()

    init {
        analyticsProfile.update()

        apiConfig
            .observeNeedConfig()
            .distinctUntilChanged()
            .onEach { needConfig ->
                _state.update { it.copy(needConfig = needConfig) }
                if (!needConfig && firstLaunch) {
                    initMain()
                }
            }
            .launchIn(viewModelScope)

        if (apiConfig.needConfig) {
            _state.update { it.copy(needConfig = true) }
        } else {
            initMain()
        }
    }

    private fun initMain() {
        firstLaunch = false
        if (authRepository.getAuthState() == AuthState.NO_AUTH) {
            authMainAnalytics.open(AnalyticsConstants.screen_main)
            router.navigateTo(Screens.Auth())
        }

        selectTab(defaultScreen)
        authRepository
            .observeAuthState()
            .onEach { updateTabsAction.set(Unit) }
            .launchIn(viewModelScope)
        _state.update { it.copy(mainLogicCompleted = true) }

        viewModelScope.launch {
            coRunCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
            coRunCatching {
                donationRepository.requestUpdate()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun onBackPressed() {
        router.exit()
    }

    fun getAuthState() = authRepository.getAuthState()

    fun selectTab(screenKey: String) {
        _state.update { it.copy(selectedTab = screenKey) }
    }

    fun submitScreenAnalytics(screen: Screen) {
        when (screen) {
            is Screens.Catalog -> catalogAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.Favorites -> favoritesAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.MainFeed -> feedAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.MainYouTube -> youtubeVideosAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.MainOther -> otherAnalytics.open(AnalyticsConstants.screen_main)
        }
    }

}
