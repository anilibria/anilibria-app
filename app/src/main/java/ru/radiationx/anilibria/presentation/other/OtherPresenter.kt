package ru.radiationx.anilibria.presentation.other

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

@InjectViewState
class OtherPresenter(
        private val router: Router,
        private val authRepository: AuthRepository
) : BasePresenter<OtherView>(router) {

    companion object {
        val MENU_FAVORITES = 0
        val MENU_TEAM = 1
        //val MENU_BID = 2
        val MENU_DONATE = 3
        val MENU_ABOUT_ANILIB = 4
        val MENU_RULES = 5

        val MENU_SETTINGS = 6

        val MENU_GROUP_VK = 7
        val MENU_CANAL_YT = 8
        val MENU_PATREON = 9
        val MENU_CANAL_TG = 10
        val MENU_CHAT_DSC = 11
        val MENU_SITE_ANILIB = 12

        val GROUP_MAIN = arrayOf(
                MENU_FAVORITES,
                MENU_TEAM,
                //MENU_BID,
                MENU_DONATE,
                MENU_ABOUT_ANILIB,
                MENU_RULES
        )

        val GROUP_SYSTEM = arrayOf(MENU_SETTINGS)

        val GROUP_LINK = arrayOf(
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

        menuMap.put(MENU_FAVORITES, OtherMenuItem(MENU_FAVORITES, "Избранное", R.drawable.ic_star))
        menuMap.put(MENU_TEAM, OtherMenuItem(MENU_TEAM, "Список команды", R.drawable.ic_account_multiple))
        //menuMap.put(MENU_BID, OtherMenuItem(MENU_BID, "Подать заявку", R.drawable.ic_account_plus))
        menuMap.put(MENU_DONATE, OtherMenuItem(MENU_DONATE, "Поддержать", R.drawable.ic_gift))
        menuMap.put(MENU_ABOUT_ANILIB, OtherMenuItem(MENU_ABOUT_ANILIB, "Об AniLibria", R.drawable.ic_information))
        menuMap.put(MENU_RULES, OtherMenuItem(MENU_RULES, "Правила", R.drawable.ic_book_open_variant))

        menuMap.put(MENU_SETTINGS, OtherMenuItem(MENU_SETTINGS, "Настройки", R.drawable.ic_settings))

        menuMap.put(MENU_GROUP_VK, OtherMenuItem(MENU_GROUP_VK, "Группа VK", R.drawable.ic_logo_vk))
        menuMap.put(MENU_CANAL_YT, OtherMenuItem(MENU_CANAL_YT, "Канал YouTube", R.drawable.ic_logo_youtube))
        menuMap.put(MENU_PATREON, OtherMenuItem(MENU_PATREON, "Patreon", R.drawable.ic_logo_patreon))
        menuMap.put(MENU_CANAL_TG, OtherMenuItem(MENU_CANAL_TG, "Канал Telegram", R.drawable.ic_logo_telegram))
        menuMap.put(MENU_CHAT_DSC, OtherMenuItem(MENU_CHAT_DSC, "Чат Discord", R.drawable.ic_logo_discord))
        menuMap.put(MENU_SITE_ANILIB, OtherMenuItem(MENU_SITE_ANILIB, "Сайт AniLibria", R.drawable.ic_anilibria))

        subscribeUser()
        updateMenuItems()
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

        GROUP_MAIN.forEach {
            if (!blockedMenu.contains(it)) {
                mainMenu.add(menuMap.getValue(it))
            }
        }

        GROUP_SYSTEM.forEach {
            if (!blockedMenu.contains(it)) {
                systemMenu.add(menuMap.getValue(it))
            }
        }

        GROUP_LINK.forEach {
            if (!blockedMenu.contains(it)) {
                linkMenu.add(menuMap.getValue(it))
            }
        }

        viewState.showItems(profileItem, listOf(mainMenu, systemMenu, linkMenu))
    }

    private fun subscribeUser() {
        authRepository.observeUser()
                .subscribe {
                    profileItem = it
                    Log.e("SUKA", "updateUser ${it.nick} : ${profileItem.nick}")
                    updateMenuItems()
                }
                .addToDisposable()
    }

    fun signOut() {
        authRepository.signOut()
        router.showSystemMessage("Данные авторизации удалены")
    }

    fun onMenuClick(item: OtherMenuItem) {
        when (item.id) {
            MENU_FAVORITES -> {
                router.navigateTo(Screens.FAVORITES)
            }
            MENU_TEAM -> {
                router.navigateTo(Screens.STATIC_PAGE, PageApi.PAGE_ID_TEAM)
            }
            MENU_DONATE -> {
                router.navigateTo(Screens.STATIC_PAGE, PageApi.PAGE_ID_DONATE)
            }
            MENU_ABOUT_ANILIB -> {
                router.navigateTo(Screens.STATIC_PAGE, PageApi.PAGE_ID_ABOUT_ANILIB)
            }
            MENU_RULES -> {
                router.navigateTo(Screens.STATIC_PAGE, PageApi.PAGE_ID_RULES)
            }
            MENU_SETTINGS -> {
                router.showSystemMessage("Кто-то спиздил настройки походу")
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
                Utils.externalLink("https://discord.gg/nZvVMfp")
            }
            MENU_SITE_ANILIB -> {
                Utils.externalLink("https://anilibria.tv/")
            }
        }
    }
}
