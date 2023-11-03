package ru.radiationx.anilibria.ui.fragments.other

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.asDataIconRes
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.domain.other.OtherMenuItem
import ru.radiationx.data.entity.domain.other.ProfileItem
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.MenuRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class OtherViewModel(
    private val router: Router,
    private val systemMessenger: SystemMessenger,
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val menuRepository: MenuRepository,
    private val systemUtils: SystemUtils,
    private val authDeviceAnalytics: AuthDeviceAnalytics,
    private val authMainAnalytics: AuthMainAnalytics,
    private val historyAnalytics: HistoryAnalytics,
    private val otherAnalytics: OtherAnalytics,
    private val settingsAnalytics: SettingsAnalytics,
    private val pageAnalytics: PageAnalytics,
    private val donationDetailAnalytics: DonationDetailAnalytics,
    private val teamsAnalytics: TeamsAnalytics,
) : ViewModel() {

    companion object {
        const val MENU_HISTORY = 0
        const val MENU_TEAM = 1
        const val MENU_DONATE = 2
        const val MENU_SETTINGS = 3
        const val MENU_OTP_CODE = 4
    }

    private val _state = MutableStateFlow(ProfileScreenState())
    val state = _state.asStateFlow()

    private val _otpEvent = EventFlow<Unit>()
    val otpEvent = _otpEvent.observe()

    private var linksMap = mutableMapOf<Int, LinkMenuItem>()

    private val profileMenu = listOf(
        OtherMenuItem(
            MENU_OTP_CODE,
            "Привязать устройство",
            R.drawable.ic_devices_other
        )
    )
    private val allMainMenu = listOf(
        OtherMenuItem(MENU_HISTORY, "История", R.drawable.ic_history),
        OtherMenuItem(MENU_TEAM, "Команда проекта", R.drawable.ic_account_multiple),
        OtherMenuItem(MENU_DONATE, "Поддержать", R.drawable.ic_gift)
    )
    private val allSystemMenu = listOf(
        OtherMenuItem(MENU_SETTINGS, "Настройки", R.drawable.ic_settings)
    )

    init {
        subscribeUpdate()
        viewModelScope.launch {
            coRunCatching {
                menuRepository.getMenu()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            coRunCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun onProfileClick() {
        viewModelScope.launch {
            if (authRepository.getAuthState() == AuthState.AUTH) {
                otherAnalytics.profileClick()
                return@launch
            }
            otherAnalytics.loginClick()
            authMainAnalytics.open(AnalyticsConstants.screen_other)
            router.navigateTo(Screens.Auth())
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun signOut() {
        otherAnalytics.logoutClick()
        GlobalScope.launch {
            coRunCatching {
                authRepository.signOut()
            }.onSuccess {
                systemMessenger.showMessage("Данные авторизации удалены")
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onMenuClick(item: OtherMenuItemState) {
        when (item.id) {
            MENU_HISTORY -> {
                otherAnalytics.historyClick()
                historyAnalytics.open(AnalyticsConstants.screen_other)
                router.navigateTo(Screens.History())
            }

            MENU_TEAM -> {
                otherAnalytics.teamClick()
                teamsAnalytics.open(AnalyticsConstants.screen_other)
                router.navigateTo(Screens.Teams())
            }

            MENU_DONATE -> {
                otherAnalytics.donateClick()
                donationDetailAnalytics.open(AnalyticsConstants.screen_other)
                router.navigateTo(Screens.DonationDetail())
            }

            MENU_SETTINGS -> {
                otherAnalytics.settingsClick()
                router.navigateTo(Screens.Settings())
            }

            MENU_OTP_CODE -> {
                settingsAnalytics.open(AnalyticsConstants.screen_other)
                otherAnalytics.authDeviceClick()
                authDeviceAnalytics.open(AnalyticsConstants.screen_other)
                _otpEvent.set(Unit)
            }

            else -> {
                linksMap[item.id]?.also { linkItem ->
                    otherAnalytics.linkClick(linkItem.title)
                    val absoluteLink = linkItem.absoluteLink
                    val pagePath = linkItem.sitePagePath
                    when {
                        absoluteLink != null -> systemUtils.externalLink(absoluteLink)
                        pagePath != null -> {
                            pageAnalytics.open(AnalyticsConstants.screen_other, pagePath)
                            router.navigateTo(Screens.StaticPage(pagePath))
                        }
                    }
                }
            }
        }
    }

    private fun subscribeUpdate() {
        combine(
            authRepository.observeAuthState(),
            authRepository.observeUser(),
            menuRepository.observeMenu()
        ) { authState, user, menu ->
            updateMenuItems(authState, user, menu)
        }.launchIn(viewModelScope)
    }

    private fun updateMenuItems(
        authState: AuthState,
        user: ProfileItem?,
        menu: List<LinkMenuItem>,
    ) {
        val linkMenu = menu.map {
            OtherMenuItem(
                id = it.hashCode(),
                title = it.title,
                icon = it.icon?.asDataIconRes() ?: R.drawable.ic_link
            )
        }
        linksMap.clear()
        linksMap.putAll(menu.associateBy { it.hashCode() })

        val profileMenu = profileMenu.let { items ->
            if (authState != AuthState.AUTH) {
                items.filterNot { it.id == MENU_OTP_CODE }
            } else {
                items
            }
        }

        val profileState = user.toState()
        val profileMenuState = profileMenu.map { it.toState() }
        val menuState = listOf(allMainMenu, allSystemMenu, linkMenu)
            .filter { it.isNotEmpty() }
            .map { itemsGroup ->
                itemsGroup.map { it.toState() }
            }
        _state.update {
            it.copy(
                profile = profileState,
                profileMenuItems = profileMenuState,
                menuItems = menuState
            )
        }
    }
}
