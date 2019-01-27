package ru.radiationx.anilibria.entity.app.search

/**
 * Created by radiationx on 24.12.17.
 */
class SearchItem {
    var id: Int = 0
    var code: String? = null
    val names = mutableListOf<String>()
    var poster: String? = null

    val title: String?
        get() = names.firstOrNull()

    val titleEng: String?
        get() = names.lastOrNull()
}
