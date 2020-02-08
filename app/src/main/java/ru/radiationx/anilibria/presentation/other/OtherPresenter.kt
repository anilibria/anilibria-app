package ru.radiationx.anilibria.presentation.other

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.app.other.LinkMenuItem
import ru.radiationx.data.entity.app.other.OtherMenuItem
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.repository.MenuRepository
import ru.radiationx.data.system.messages.SystemMessenger
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class OtherPresenter @Inject constructor(
        private val router: Router,
        private val systemMessenger: SystemMessenger,
        private val authRepository: AuthRepository,
        private val errorHandler: IErrorHandler,
        private val apiConfig: ApiConfig,
        private val menuRepository: MenuRepository
) : BasePresenter<OtherView>(router) {

    companion object {
        const val MENU_HISTORY = 0
        const val MENU_TEAM = 1
        const val MENU_DONATE = 2
        const val MENU_SETTINGS = 3
    }

    private var currentProfileItem: ProfileItem = authRepository.getUser()
    private var currentLinkMenuItems = mutableListOf<LinkMenuItem>()
    private var linksMap = mutableMapOf<Int, LinkMenuItem>()

    private val allMainMenu = mutableListOf<OtherMenuItem>()
    private val allSystemMenu = mutableListOf<OtherMenuItem>()
    private val allLinkMenu = mutableListOf<OtherMenuItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        allMainMenu.add(OtherMenuItem(MENU_HISTORY, "История", R.drawable.ic_history))
        allMainMenu.add(OtherMenuItem(MENU_TEAM, "Список команды", R.drawable.ic_account_multiple))
        allMainMenu.add(OtherMenuItem(MENU_DONATE, "Поддержать", R.drawable.ic_gift))

        allSystemMenu.add(OtherMenuItem(MENU_SETTINGS, "Настройки", R.drawable.ic_settings))

        menuRepository
                .getMenu()
                .subscribe({}, {
                    it.printStackTrace()
                })
                .addToDisposable()
        subscribeUpdate()
        updateMenuItems()
    }


    override fun attachView(view: OtherView?) {
        super.attachView(view)
        authRepository
                .loadUser()
                .subscribe({}, {})
                .addToDisposable()
    }


    fun openAuth() {
        if (currentProfileItem.authState == AuthState.AUTH) {
            return
        }
        router.navigateTo(Screens.Auth())
    }

    fun signOut() {
        val disposable = authRepository
                .signOut()
                .subscribe({
                    systemMessenger.showMessage("Данные авторизации удалены")
                }, {
                    errorHandler.handle(it)
                })
    }

    fun onMenuClick(item: OtherMenuItem) {
        when (item.id) {
            MENU_HISTORY -> router.navigateTo(Screens.History())
            MENU_TEAM -> router.navigateTo(Screens.StaticPage(PageApi.PAGE_PATH_TEAM))
            MENU_DONATE -> Utils.externalLink("${apiConfig.siteUrl}/${PageApi.PAGE_PATH_DONATE}")
            MENU_SETTINGS -> router.navigateTo(Screens.Settings())
            else -> {
                linksMap[item.id]?.also { linkItem ->
                    val absoluteLink = linkItem.absoluteLink
                    val pagePath = linkItem.sitePagePath
                    when {
                        absoluteLink != null -> Utils.externalLink(absoluteLink)
                        pagePath != null -> router.navigateTo(Screens.StaticPage(pagePath))
                    }
                }
            }
        }
    }

    private fun subscribeUpdate() {
        authRepository
                .observeUser()
                .subscribe {
                    currentProfileItem = it
                    updateMenuItems()
                }
                .addToDisposable()

        menuRepository
                .observeMenu()
                .subscribe { linkItems ->
                    currentLinkMenuItems.clear()
                    currentLinkMenuItems.addAll(linkItems)
                    allLinkMenu.clear()
                    allLinkMenu.addAll(linkItems.map {
                        OtherMenuItem(it.hashCode(), it.title, getResIconByType(it.icon))
                    })
                    linksMap.clear()
                    linksMap.putAll(linkItems.associateBy { it.hashCode() })
                    updateMenuItems()
                }
                .addToDisposable()
    }

    private fun updateMenuItems() {
        // Для фильтрации, если вдруг понадобится добавить
        val mainMenu = allMainMenu.toMutableList()
        val systemMenu = allSystemMenu.toMutableList()
        val linkMenu = allLinkMenu.toMutableList()

        viewState.showItems(currentProfileItem, listOf(mainMenu, systemMenu, linkMenu).filter { it.isNotEmpty() })
    }

    private fun getResIconByType(type: String?): Int = when (type) {
        LinkMenuItem.IC_VK -> R.drawable.ic_logo_vk
        LinkMenuItem.IC_YOUTUBE -> R.drawable.ic_logo_youtube
        LinkMenuItem.IC_PATREON -> R.drawable.ic_logo_patreon
        LinkMenuItem.IC_TELEGRAM -> R.drawable.ic_logo_telegram
        LinkMenuItem.IC_DISCORD -> R.drawable.ic_logo_discord
        LinkMenuItem.IC_ANILIBRIA -> R.drawable.ic_anilibria
        LinkMenuItem.IC_INFO -> R.drawable.ic_information
        LinkMenuItem.IC_RULES -> R.drawable.ic_book_open_variant
        LinkMenuItem.IC_PERSON -> R.drawable.ic_person
        LinkMenuItem.IC_SITE -> R.drawable.ic_link
        else -> R.drawable.ic_link
    }
}
