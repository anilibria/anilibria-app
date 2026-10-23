package tv.aniliberty.next

import java.net.URLEncoder

data class AccountFilter(
    val genres: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
    val years: Set<String> = emptySet(),
    val ages: Set<String> = emptySet(),
    val sorting: String = "CREATED_AT_DESC",
) {
    val count: Int
        get() = genres.size + types.size + years.size + ages.size
}

internal fun isAccountList(screen: Screen) =
    screen is Screen.Favorites || screen is Screen.Collections

internal fun accountListQuery(screen: Screen) =
    when (screen) {
        is Screen.Favorites -> screen.query
        is Screen.Collections -> screen.query
        else -> ""
    }

internal fun accountListFilter(screen: Screen) =
    when (screen) {
        is Screen.Favorites -> screen.filter
        is Screen.Collections -> screen.filter
        else -> AccountFilter()
    }

internal fun withAccountQuery(screen: Screen, query: String): Screen =
    when (screen) {
        is Screen.Favorites -> screen.copy(query = query)
        is Screen.Collections -> screen.copy(query = query)
        else -> screen
    }

internal fun withAccountFilter(screen: Screen, filter: AccountFilter): Screen =
    when (screen) {
        is Screen.Favorites -> screen.copy(filter = filter)
        is Screen.Collections -> screen.copy(filter = filter)
        else -> screen
    }

internal fun accountReleasePath(screen: Screen, page: Int): String {
    require(isAccountList(screen) && page > 0)
    val f = accountListFilter(screen)
    val values =
        linkedMapOf(
            "page" to page.toString(),
            "limit" to "30",
            "exclude" to "torrents,members,sponsors",
        )
    val section = if (screen is Screen.Favorites) "favorites" else "collections"
    if (screen is Screen.Collections) values["type_of_collection"] = screen.type.name
    if (screen is Screen.Favorites) values["f[sorting]"] = f.sorting
    accountListQuery(screen).trim().takeIf { it.isNotBlank() }?.let { values["f[search]"] = it }
    listOf("genres" to f.genres, "types" to f.types, "years" to f.years, "age_ratings" to f.ages)
        .forEach { (key, selected) ->
            if (selected.isNotEmpty()) values["f[$key]"] = selected.sorted().joinToString(",")
        }
    return "/accounts/users/me/$section/releases?" +
        values.entries.joinToString("&") {
            URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8")
        }
}

internal data class AccountReleaseState(
    val items: List<Release> = emptyList(),
    val page: Int = 0,
    val more: Boolean = false,
    val total: Int? = null,
    val loading: Boolean = false,
    val error: String? = null,
)
