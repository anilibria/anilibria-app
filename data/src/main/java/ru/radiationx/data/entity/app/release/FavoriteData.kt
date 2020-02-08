package ru.radiationx.data.entity.app.release

import ru.radiationx.data.entity.app.Paginated

/**
 * Created by radiationx on 26.01.18.
 */
class FavoriteData {
    lateinit var sessId: String
    lateinit var items: Paginated<List<ReleaseItem>>
}