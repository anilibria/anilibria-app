package ru.radiationx.anilibria.presentation.main

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
@InjectViewState
class MainPresenter @Inject constructor(
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
) : BasePresenter<MainView>(router) {

    var defaultScreen = Screens.MainFeed().screenKey!!

    private var firstLaunch = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        analyticsProfile.update()

        apiConfig
            .observeNeedConfig()
            .distinctUntilChanged()
            .onEach {
                if (it) {
                    viewState.showConfiguring()
                } else {
                    viewState.hideConfiguring()
                    if (firstLaunch) {
                        initMain()
                    }
                }
            }
            .launchIn(presenterScope)

        if (apiConfig.needConfig) {
            viewState.showConfiguring()
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
            .onEach { viewState.updateTabs() }
            .launchIn(presenterScope)
        viewState.onMainLogicCompleted()

        presenterScope.launch {
            runCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
            runCatching {
                donationRepository.requestUpdate()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun getAuthState() = authRepository.getAuthState()

    fun selectTab(screenKey: String) {
        viewState.highlightTab(screenKey)
    }

    fun submitScreenAnalytics(screen: Screen) {
        when (screen) {
            is Screens.ReleasesSearch -> catalogAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.Favorites -> favoritesAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.MainFeed -> feedAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.MainYouTube -> youtubeVideosAnalytics.open(AnalyticsConstants.screen_main)
            is Screens.MainOther -> otherAnalytics.open(AnalyticsConstants.screen_main)
        }
    }

}
