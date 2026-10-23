package tv.aniliberty.next

sealed interface Screen {
    data object Welcome : Screen

    data object Home : Screen

    data class Favorites(val query: String = "", val filter: AccountFilter = AccountFilter()) :
        Screen

    data class Collections(
        val type: CollectionType = CollectionType.WATCHING,
        val query: String = "",
        val filter: AccountFilter = AccountFilter(),
    ) : Screen

    data object Settings : Screen

    data class AccountSettings(val section: String) : Screen

    data object Profile : Screen

    data object AccountSessions : Screen

    data object AccountHistory : Screen

    data class Library(val ids: List<Int>, val title: String = "Моя библиотека") : Screen

    data class Catalog(
        val query: String = "",
        val page: Int = 1,
        val genre: Int? = null,
        val title: String? = null,
        val filters: CatalogFilter = CatalogFilter(),
    ) : Screen

    data class Section(val kind: HomeSection, val page: Int = 1) : Screen

    data class Franchise(val id: String, val title: String) : Screen

    data class Detail(val id: Int, val tab: Int = 0) : Screen
}
