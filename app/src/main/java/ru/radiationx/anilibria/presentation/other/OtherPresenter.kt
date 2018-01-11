package ru.radiationx.anilibria.presentation.other

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

@InjectViewState
class OtherPresenter(
        private val router: Router,
        private val authRepository: AuthRepository
) : BasePresenter<OtherView>(router) {

    private val profileItem: ProfileItem = ProfileItem()
    private val mainMenu = mutableListOf<OtherMenuItem>()
    private val systemMenu = mutableListOf<OtherMenuItem>()
    private val linkMenu = mutableListOf<OtherMenuItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        mainMenu.add(OtherMenuItem("Избранное", R.drawable.ic_star))
        mainMenu.add(OtherMenuItem("Список команды", R.drawable.ic_account_multiple))
        mainMenu.add(OtherMenuItem("Подать заявку", R.drawable.ic_account_plus))
        mainMenu.add(OtherMenuItem("Поддержать", R.drawable.ic_gift))
        mainMenu.add(OtherMenuItem("Об AniLibria", R.drawable.ic_information))
        mainMenu.add(OtherMenuItem("Правила", R.drawable.ic_book_open_variant))

        systemMenu.add(OtherMenuItem("Настройки", R.drawable.ic_settings))

        linkMenu.add(OtherMenuItem("Группа VK", R.drawable.ic_logo_vk))
        linkMenu.add(OtherMenuItem("Канал YouTube", R.drawable.ic_logo_youtube))
        linkMenu.add(OtherMenuItem("Patreon", R.drawable.ic_logo_patreon))
        linkMenu.add(OtherMenuItem("Канал Telegram", R.drawable.ic_logo_telegram))
        linkMenu.add(OtherMenuItem("Чат Discord", R.drawable.ic_logo_discord))

        viewState.showItems(profileItem, listOf(mainMenu, systemMenu, linkMenu))
    }
}
