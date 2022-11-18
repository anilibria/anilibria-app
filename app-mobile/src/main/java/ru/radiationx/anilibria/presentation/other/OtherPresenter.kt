package ru.radiationx.anilibria.presentation.other

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.asDataIconRes
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.ui.fragments.other.ProfileScreenState
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.domain.other.OtherMenuItem
import ru.radiationx.data.entity.domain.other.ProfileItem
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.MenuRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class OtherPresenter @Inject constructor(
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
    private val teamsAnalytics: TeamsAnalytics
) : BasePresenter<OtherView>(router) {

    companion object {
        const val MENU_HISTORY = 0
        const val MENU_TEAM = 1
        const val MENU_DONATE = 2
        const val MENU_SETTINGS = 3
        const val MENU_OTP_CODE = 4
    }

    private val stateController = StateController(ProfileScreenState())

    private var currentProfileItem: ProfileItem? = authRepository.getUser()
    private var currentLinkMenuItems = mutableListOf<LinkMenuItem>()
    private var linksMap = mutableMapOf<Int, LinkMenuItem>()

    private val profileMenu = mutableListOf<OtherMenuItem>()
    private val allMainMenu = mutableListOf<OtherMenuItem>()
    private val allSystemMenu = mutableListOf<OtherMenuItem>()
    private val allLinkMenu = mutableListOf<OtherMenuItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

        profileMenu.add(
            OtherMenuItem(
                MENU_OTP_CODE,
                "Привязать устройство",
                R.drawable.ic_devices_other
            )
        )

        allMainMenu.add(OtherMenuItem(MENU_HISTORY, "История", R.drawable.ic_history))
        allMainMenu.add(OtherMenuItem(MENU_TEAM, "Команда проекта", R.drawable.ic_account_multiple))
        allMainMenu.add(OtherMenuItem(MENU_DONATE, "Поддержать", R.drawable.ic_gift))

        allSystemMenu.add(OtherMenuItem(MENU_SETTINGS, "Настройки", R.drawable.ic_settings))

        presenterScope.launch {
            runCatching {
                menuRepository.getMenu()
            }.onFailure {
                Timber.e(it)
            }
        }
        subscribeUpdate()
        updateMenuItems()
    }

    override fun attachView(view: OtherView?) {
        super.attachView(view)
        presenterScope.launch {
            runCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun onProfileClick() {
        if (authRepository.getAuthState() == AuthState.AUTH) {
            otherAnalytics.profileClick()
            return
        }
        otherAnalytics.loginClick()
        authMainAnalytics.open(AnalyticsConstants.screen_other)
        router.navigateTo(Screens.Auth())
    }

    fun signOut() {
        otherAnalytics.logoutClick()
        GlobalScope.launch {
            runCatching {
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
                viewState.showOtpCode()
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
        authRepository
            .observeUser()
            .onEach {
                currentProfileItem = it
                updateMenuItems()
            }
            .launchIn(presenterScope)

        menuRepository
            .observeMenu()
            .onEach { linkItems ->
                currentLinkMenuItems.clear()
                currentLinkMenuItems.addAll(linkItems)
                allLinkMenu.clear()
                allLinkMenu.addAll(linkItems.map {
                    OtherMenuItem(
                        id = it.hashCode(),
                        title = it.title,
                        icon = it.icon?.asDataIconRes() ?: R.drawable.ic_link
                    )
                })
                linksMap.clear()
                linksMap.putAll(linkItems.associateBy { it.hashCode() })
                updateMenuItems()
            }
            .launchIn(presenterScope)
    }

    private fun updateMenuItems() {
        // Для фильтрации, если вдруг понадобится добавить
        val profileMenu = profileMenu.toMutableList()
        val mainMenu = allMainMenu.toMutableList()
        val systemMenu = allSystemMenu.toMutableList()
        val linkMenu = allLinkMenu.toMutableList()

        if (authRepository.getAuthState() != AuthState.AUTH) {
            profileMenu.removeAll { it.id == MENU_OTP_CODE }
        }

        val profileState = currentProfileItem.toState()
        val profileMenuState = profileMenu.map { it.toState() }
        val menuState = listOf(mainMenu, systemMenu, linkMenu)
            .filter { it.isNotEmpty() }
            .map { itemsGroup ->
                itemsGroup.map { it.toState() }
            }
        stateController.updateState {
            it.copy(
                profile = profileState,
                profileMenuItems = profileMenuState,
                menuItems = menuState
            )
        }
    }
}
