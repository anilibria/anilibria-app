package ru.radiationx.anilibria.presentation.other

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
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
        private val apiConfig: ApiConfig
) : BasePresenter<OtherView>(router) {

    companion object {
        const val MENU_FAVORITES = 0
        const val MENU_TEAM = 1
        //val MENU_BID = 2
        const val MENU_DONATE = 3
        const val MENU_ABOUT_ANILIB = 4
        const val MENU_RULES = 5


        const val MENU_SETTINGS = 6

        const val MENU_GROUP_VK = 7
        const val MENU_CANAL_YT = 8
        const val MENU_PATREON = 9
        const val MENU_CANAL_TG = 10
        const val MENU_CHAT_DSC = 11
        const val MENU_SITE_ANILIB = 12

        const val MENU_HISTORY = 13

        const val MENU_RIGHT_HOLDERS = 14

        val GROUP_MAIN = arrayOf(
                //MENU_FAVORITES,
                MENU_HISTORY,
                MENU_TEAM,
                //MENU_BID,
                MENU_DONATE
                //MENU_ABOUT_ANILIB,
                //MENU_RULES
        )

        val GROUP_SYSTEM = arrayOf(MENU_SETTINGS)

        val GROUP_LINK = arrayOf(
                MENU_RIGHT_HOLDERS,
                MENU_GROUP_VK,
                MENU_CANAL_YT,
                MENU_PATREON,
                MENU_CANAL_TG,
                MENU_CHAT_DSC,
                MENU_SITE_ANILIB
        )
    }

    private val menuMap = mutableMapOf<Int, OtherMenuItem>()

    private val blockedMenu = mutableListOf<Int>()

    private var profileItem: ProfileItem = authRepository.getUser()
    private val mainMenu = mutableListOf<OtherMenuItem>()
    private val systemMenu = mutableListOf<OtherMenuItem>()
    private val linkMenu = mutableListOf<OtherMenuItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        menuMap[MENU_FAVORITES] = OtherMenuItem(MENU_FAVORITES, "Избранное", R.drawable.ic_star)
        menuMap[MENU_HISTORY] = OtherMenuItem(MENU_HISTORY, "История", R.drawable.ic_history)
        menuMap[MENU_TEAM] = OtherMenuItem(MENU_TEAM, "Список команды", R.drawable.ic_account_multiple)
        //menuMap.put(MENU_BID, OtherMenuItem(MENU_BID, "Подать заявку", R.drawable.ic_account_plus))
        menuMap[MENU_DONATE] = OtherMenuItem(MENU_DONATE, "Поддержать", R.drawable.ic_gift)
        menuMap[MENU_ABOUT_ANILIB] = OtherMenuItem(MENU_ABOUT_ANILIB, "Об AniLibria", R.drawable.ic_information)
        menuMap[MENU_RULES] = OtherMenuItem(MENU_RULES, "Правила", R.drawable.ic_book_open_variant)

        menuMap[MENU_SETTINGS] = OtherMenuItem(MENU_SETTINGS, "Настройки", R.drawable.ic_settings)

        menuMap[MENU_RIGHT_HOLDERS] = OtherMenuItem(MENU_RIGHT_HOLDERS, "Правообладателям", R.drawable.ic_copyright)
        menuMap[MENU_GROUP_VK] = OtherMenuItem(MENU_GROUP_VK, "Группа VK", R.drawable.ic_logo_vk)
        menuMap[MENU_CANAL_YT] = OtherMenuItem(MENU_CANAL_YT, "Канал YouTube", R.drawable.ic_logo_youtube)
        menuMap[MENU_PATREON] = OtherMenuItem(MENU_PATREON, "Patreon", R.drawable.ic_logo_patreon)
        menuMap[MENU_CANAL_TG] = OtherMenuItem(MENU_CANAL_TG, "Канал Telegram", R.drawable.ic_logo_telegram)
        menuMap[MENU_CHAT_DSC] = OtherMenuItem(MENU_CHAT_DSC, "Чат Discord", R.drawable.ic_logo_discord)
        menuMap[MENU_SITE_ANILIB] = OtherMenuItem(MENU_SITE_ANILIB, "Сайт AniLibria", R.drawable.ic_anilibria)

        subscribeUser()
        updateMenuItems()
    }

    override fun attachView(view: OtherView?) {
        super.attachView(view)
        authRepository
                .loadUser()
                .subscribe({}, {})
                .addToDisposable()
    }

    private fun updateMenuItems() {
        mainMenu.clear()
        systemMenu.clear()
        linkMenu.clear()

        if (profileItem.authState == AuthState.AUTH) {
            blockedMenu.remove(MENU_FAVORITES)
        } else {
            blockedMenu.add(MENU_FAVORITES)
        }

        if (Api.STORE_APP_IDS.contains(BuildConfig.APPLICATION_ID)) {
            blockedMenu.remove(MENU_RIGHT_HOLDERS)
        } else {
            blockedMenu.add(MENU_RIGHT_HOLDERS)
        }

        GROUP_MAIN.forEach {
            if (!blockedMenu.contains(it) && menuMap.contains(it)) {
                mainMenu.add(menuMap.getValue(it))
            }
        }

        GROUP_SYSTEM.forEach {
            if (!blockedMenu.contains(it) && menuMap.contains(it)) {
                systemMenu.add(menuMap.getValue(it))
            }
        }

        GROUP_LINK.forEach {
            if (!blockedMenu.contains(it) && menuMap.contains(it)) {
                linkMenu.add(menuMap.getValue(it))
            }
        }

        viewState.showItems(profileItem, listOf(mainMenu, systemMenu, linkMenu))
    }

    private fun subscribeUser() {
        authRepository.observeUser()
                .subscribe {
                    profileItem = it
                    updateMenuItems()
                }
                .addToDisposable()
    }

    fun signOut() {
        authRepository
                .signOut()
                .subscribe({
                    systemMessenger.showMessage("Данные авторизации удалены")
                }, {
                    errorHandler.handle(it)
                })
    }

    fun onMenuClick(item: OtherMenuItem) {
        when (item.id) {
            MENU_FAVORITES -> {
                router.navigateTo(Screens.Favorites())
            }
            MENU_HISTORY -> {
                router.navigateTo(Screens.History())
            }
            MENU_TEAM -> {
                router.navigateTo(Screens.StaticPage(PageApi.PAGE_ID_TEAM))
            }
            MENU_DONATE -> {
                //router.navigateTo(Screens.StaticPage(PageApi.PAGE_ID_DONATE))
                Utils.externalLink("${apiConfig.siteUrl}/${PageApi.PAGE_ID_DONATE}")
            }
            MENU_ABOUT_ANILIB -> {
                router.navigateTo(Screens.StaticPage(PageApi.PAGE_ID_ABOUT_ANILIB))
            }
            MENU_RULES -> {
                router.navigateTo(Screens.StaticPage(PageApi.PAGE_ID_RULES))
            }
            MENU_SETTINGS -> {
                router.navigateTo(Screens.Settings())
            }
            MENU_GROUP_VK -> {
                Utils.externalLink("https://vk.com/anilibria")
            }
            MENU_CANAL_YT -> {
                Utils.externalLink("https://youtube.com/channel/UCuF8ghQWaa7K-28llm-K3Zg")
            }
            MENU_PATREON -> {
                Utils.externalLink("https://patreon.com/anilibria")
            }
            MENU_CANAL_TG -> {
                Utils.externalLink("https://t.me/anilibria_tv")
            }
            MENU_CHAT_DSC -> {
                Utils.externalLink("https://discordapp.com/invite/anilibria")
            }
            MENU_SITE_ANILIB -> {
                Utils.externalLink(apiConfig.siteUrl)
            }
            MENU_RIGHT_HOLDERS -> {
                Utils.externalLink("${apiConfig.siteUrl}/${PageApi.PAGE_ID_RIGHT_HOLDERS}")
            }
        }
    }

    fun openAuth() {
        if (profileItem.authState == AuthState.AUTH) {
            //systemMessenger.showMessage("Просмотр профиля недоступен")
            return
        }
        router.navigateTo(Screens.Auth())
    }
}
