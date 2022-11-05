package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.shared.ktx.android.mapObjects
import ru.radiationx.shared.ktx.android.nullGet
import toothpick.InjectConstructor

@InjectConstructor
class PaginationParser {

    fun <T> parse(jsonObject: JSONObject, mapper: (JSONObject) -> T): Paginated<T> {
        val jsonItems = jsonObject.getJSONArray("items")
        val items = jsonItems.mapObjects { mapper.invoke(it) }
        val jsonNav = jsonObject.getJSONObject("pagination")
        return Paginated(
            data = items,
            page = jsonNav.nullGet("page")?.toString()?.toInt(),
            allPages = jsonNav.nullGet("allPages")?.toString()?.toInt(),
            perPage = jsonNav.nullGet("perPage")?.toString()?.toInt(),
            allItems = jsonNav.nullGet("allItems")?.toString()?.toInt()
        )
    }
}