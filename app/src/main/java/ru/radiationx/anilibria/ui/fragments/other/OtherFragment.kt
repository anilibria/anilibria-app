package ru.radiationx.anilibria.ui.fragments.other

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_other.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.OtherMenuItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.other.adapter.OtherAdapter


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseFragment() {

    private val adapter = OtherAdapter()

    override fun getLayoutResource(): Int = View.NO_ID

    override fun getBaseLayout(): Int = R.layout.fragment_other

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@OtherFragment.adapter
        }
        val mainMenu = mutableListOf<OtherMenuItem>()
        mainMenu.add(OtherMenuItem("Избранное", R.drawable.ic_star))
        mainMenu.add(OtherMenuItem("Список команды", R.drawable.ic_account_multiple))
        mainMenu.add(OtherMenuItem("Подать заявку", R.drawable.ic_account_plus))
        mainMenu.add(OtherMenuItem("Поддержать", R.drawable.ic_gift))
        mainMenu.add(OtherMenuItem("Об AniLibria", R.drawable.ic_information))
        mainMenu.add(OtherMenuItem("Правила", R.drawable.ic_book_open_variant))

        val systemMenu = mutableListOf<OtherMenuItem>()
        systemMenu.add(OtherMenuItem("Настройки", R.drawable.ic_settings))

        val linkMenu = mutableListOf<OtherMenuItem>()
        linkMenu.add(OtherMenuItem("Группа VK", R.drawable.ic_logo_vk))
        linkMenu.add(OtherMenuItem("Канал YouTube", R.drawable.ic_logo_youtube))
        linkMenu.add(OtherMenuItem("Patreon", R.drawable.ic_logo_patreon))
        linkMenu.add(OtherMenuItem("Канал Telegram", R.drawable.ic_logo_telegram))
        linkMenu.add(OtherMenuItem("Чат Discord", R.drawable.ic_logo_discord))

        adapter
                .addProfile()
                .addMenu(mainMenu)
                .addMenu(systemMenu)
                .addMenu(linkMenu)
                .notifyDataSetChanged()
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
