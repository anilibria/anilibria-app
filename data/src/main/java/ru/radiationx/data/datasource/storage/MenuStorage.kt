package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.MenuHolder
import ru.radiationx.data.entity.domain.other.DataIcons
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

class MenuStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : MenuHolder {

    companion object {
        private const val LOCAL_MENU_KEY = "data.local_menu"
    }

    private val defaultLocalMenu = listOf(
        LinkMenuItem(
            "Группа VK",
            absoluteLink = "https://vk.com/anilibria",
            icon = DataIcons.VK
        ),
        LinkMenuItem(
            "Канал YouTube",
            absoluteLink = "https://youtube.com/channel/UCuF8ghQWaa7K-28llm-K3Zg",
            icon = DataIcons.YOUTUBE
        ),
        LinkMenuItem(
            "Patreon",
            absoluteLink = "https://patreon.com/anilibria",
            icon = DataIcons.PATREON
        ),
        LinkMenuItem(
            "Канал Telegram",
            absoluteLink = "https://t.me/anilibria_tv",
            icon = DataIcons.TELEGRAM
        ),
        LinkMenuItem(
            "Чат Discord",
            absoluteLink = "https://discord.gg/Kdr5sNw",
            icon = DataIcons.DISCORD
        ),
        LinkMenuItem(
            "Сайт AniLibria",
            absoluteLink = "https://www.anilibria.tv/",
            icon = DataIcons.ANILIBRIA
        )
    )

    private val localMenuRelay = SuspendMutableStateFlow {
        loadAll()
    }

    override fun observe(): Flow<List<LinkMenuItem>> = localMenuRelay

    override suspend fun save(items: List<LinkMenuItem>) {
        localMenuRelay.setValue(items.toList())
        saveAll()
    }

    override suspend fun get(): List<LinkMenuItem> = localMenuRelay.getValue()

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonMenu = JSONArray()
            localMenuRelay.getValue().forEach {
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
    }

    private suspend fun loadAll(): List<LinkMenuItem> {
        return withContext(Dispatchers.IO) {
            val result = defaultLocalMenu.toMutableList()
            sharedPreferences.getString(LOCAL_MENU_KEY, null)?.also { savedMenu ->
                val jsonMenu = JSONArray(savedMenu)
                result.clear()
                (0 until jsonMenu.length()).forEach { index ->
                    jsonMenu.getJSONObject(index).also {
                        result.add(
                            LinkMenuItem(
                                it.getString("title"),
                                it.nullString("absoluteLink"),
                                it.nullString("sitePagePath"),
                                it.nullString("icon")
                            )
                        )
                    }
                }
            }
            result
        }
    }
}