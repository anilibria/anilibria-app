package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.DataPreferences
import ru.radiationx.data.entity.app.other.LinkMenuItem
import ru.radiationx.anilibria.extension.nullString
import ru.radiationx.anilibria.model.data.holders.MenuHolder
import javax.inject.Inject

class MenuStorage @Inject constructor(
        @DataPreferences private val sharedPreferences: SharedPreferences
) : MenuHolder {

    companion object {
        private const val LOCAL_MENU_KEY = "data.local_menu"
    }

    private val localMenu = mutableListOf(
            LinkMenuItem("Группа VK", absoluteLink = "https://vk.com/anilibria", icon = LinkMenuItem.IC_VK),
            LinkMenuItem("Канал YouTube", absoluteLink = "https://youtube.com/channel/UCuF8ghQWaa7K-28llm-K3Zg", icon = LinkMenuItem.IC_YOUTUBE),
            LinkMenuItem("Patreon", absoluteLink = "https://patreon.com/anilibria", icon = LinkMenuItem.IC_PATREON),
            LinkMenuItem("Канал Telegram", absoluteLink = "https://t.me/anilibria_tv", icon = LinkMenuItem.IC_TELEGRAM),
            LinkMenuItem("Чат Discord", absoluteLink = "https://discord.gg/Kdr5sNw", icon = LinkMenuItem.IC_DISCORD),
            LinkMenuItem("Сайт AniLibria", absoluteLink = "https://www.anilibria.tv/", icon = LinkMenuItem.IC_ANILIBRIA)
    )
    private val localMenuRelay = BehaviorRelay.createDefault<List<LinkMenuItem>>(localMenu)

    init {
        loadAll()
    }

    override fun observe(): Observable<List<LinkMenuItem>> = localMenuRelay.hide()

    override fun save(items: List<LinkMenuItem>) {
        localMenu.clear()
        localMenu.addAll(items)
        saveAll()
        localMenuRelay.accept(localMenu)
    }

    override fun get(): List<LinkMenuItem> = localMenu

    private fun saveAll() {
        val jsonMenu = JSONArray()
        localMenu.forEach {
            jsonMenu.put(JSONObject().apply {
                put("title", it.title)
                put("absoluteLink", it.absoluteLink)
                put("sitePagePath", it.sitePagePath)
                put("icon", it.icon)
            })
        }
        sharedPreferences
                .edit()
                .putString(LOCAL_MENU_KEY, jsonMenu.toString())
                .apply()
    }

    private fun loadAll() {
        sharedPreferences.getString(LOCAL_MENU_KEY, null)?.also { savedMenu ->
            val jsonMenu = JSONArray(savedMenu)
            localMenu.clear()
            (0 until jsonMenu.length()).forEach { index ->
                jsonMenu.getJSONObject(index).also {
                    localMenu.add(LinkMenuItem(
                            it.getString("title"),
                            it.nullString("absoluteLink"),
                            it.nullString("sitePagePath"),
                            it.nullString("sitePagePath")
                    ))
                }
            }
        }
        localMenuRelay.accept(localMenu)
    }
}