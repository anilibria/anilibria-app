package ru.radiationx.anilibria.ui.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.data.app.ads.AdsConfigRepository
import ru.radiationx.data.app.ads.models.AdsConfig
import ru.radiationx.data.app.donation.DonationRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject


data class MainScreenState(
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
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    fun init() {
        analyticsProfile.update()
        initMain()
    }

    private fun initMain() {
        viewModelScope.launch {
            if (authRepository.getAuthState() == AuthState.NO_AUTH) {
                authMainAnalytics.open(AnalyticsConstants.screen_main)
                router.navigateTo(Screens.Auth())
            }
            _state.update { it.copy(mainLogicCompleted = true) }
        }

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
}
