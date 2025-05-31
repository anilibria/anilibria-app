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
import ru.radiationx.data.entity.common.toAbsoluteUrl
import ru.radiationx.data.entity.common.toRelativeUrl
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
            title = "Группа VK",
            link = "https://vk.com/anilibria".toAbsoluteUrl(),
            icon = DataIcons.VK
        ),
        LinkMenuItem(
            title = "Канал YouTube",
            link = "https://youtube.com/channel/UCuF8ghQWaa7K-28llm-K3Zg".toAbsoluteUrl(),
            icon = DataIcons.YOUTUBE
        ),
        LinkMenuItem(
            title = "Patreon",
            link = "https://patreon.com/anilibria".toAbsoluteUrl(),
            icon = DataIcons.PATREON
        ),
        LinkMenuItem(
            title = "Канал Telegram",
            link = "https://t.me/anilibria_tv".toAbsoluteUrl(),
            icon = DataIcons.TELEGRAM
        ),
        LinkMenuItem(
            title = "Чат Discord",
            link = "https://discord.gg/Kdr5sNw".toAbsoluteUrl(),
            icon = DataIcons.DISCORD
        ),
        LinkMenuItem(
            title = "Сайт AniLibria",
            link = "https://www.anilibria.tv/".toAbsoluteUrl(),
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
                    put("absoluteLink", it.link?.raw)
                    put("sitePagePath", it.pagePath?.raw)
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
                                it.nullString("absoluteLink")?.toAbsoluteUrl(),
                                it.nullString("sitePagePath")?.toRelativeUrl(),
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