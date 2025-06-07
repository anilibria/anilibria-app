package ru.radiationx.anilibria.ui.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.FavoritesAnalytics
import ru.radiationx.data.analytics.features.FeedAnalytics
import ru.radiationx.data.analytics.features.OtherAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.data.app.ads.AdsConfigRepository
import ru.radiationx.data.app.ads.models.AdsConfig
import ru.radiationx.data.app.donation.DonationRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject


data class MainScreenState(
    val selectedTab: String? = null,
    val mainLogicCompleted: Boolean = false,
    val adsConfig: AdsConfig? = null,
)

class MainViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val donationRepository: DonationRepository,
    private val adsConfigRepository: AdsConfigRepository,
    private val analyticsProfile: AnalyticsProfile,
    private val authMainAnalytics: AuthMainAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val favoritesAnalytics: FavoritesAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics,
    private val otherAnalytics: OtherAnalytics,
) : ViewModel() {

    // todo api2 revert
    private val defaultScreen = Screens.Catalog().screenKey

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    val updateTabsAction = EventFlow<Unit>()

    fun init(savedScreen: String?) {
        analyticsProfile.update()
        initMain(savedScreen)
    }

    private fun initMain(savedScreen: String?) {

        // todo TR-274 move in scope after refactor screen
        runBlocking {
            if (authRepository.getAuthState() == AuthState.NO_AUTH) {
                authMainAnalytics.open(AnalyticsConstants.screen_main)
                router.navigateTo(Screens.Auth())
            }
        }


        selectTab(savedScreen ?: defaultScreen)
        authRepository
            .observeAuthState()
            .onEach { updateTabsAction.set(Unit) }
            .launchIn(viewModelScope)
        _state.update { it.copy(mainLogicCompleted = true) }

        viewModelScope.launch {
            coRunCatching {
                val config = adsConfigRepository.getConfig()
                _state.update { it.copy(adsConfig = config) }
            }.onFailure {
                Timber.e(it)
            }
        }
        viewModelScope.launch {
            coRunCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
        }
        viewModelScope.launch {
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

    // todo TR-274 refactor screen with no-getter viewmodel
    fun getAuthState() = runBlocking { authRepository.getAuthState() }

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
