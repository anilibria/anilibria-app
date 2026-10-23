package tv.aniliberty.next

enum class CollectionType(val title: String, val icon: Int, val color: Int) {
    PLANNED("В планах", R.drawable.ic_collection_planned, 0xFFB39DDB.toInt()),
    WATCHING("Смотрю", R.drawable.ic_collection_watching, 0xFF4FC3F7.toInt()),
    WATCHED("Просмотрено", R.drawable.ic_collection_watched, 0xFF66BB6A.toInt()),
    POSTPONED("Отложено", R.drawable.ic_collection_postponed, 0xFFFFB74D.toInt()),
    ABANDONED("Брошено", R.drawable.ic_collection_abandoned, 0xFFEF5350.toInt()),
}

object CollectionVisuals {
    val FAVORITE_COLOR = 0xFFFFD166.toInt()
    val HISTORY_COLOR = 0xFF90A4AE.toInt()
}

data class AccountProfile(
    val id: Int,
    val name: String,
    val login: String = "",
    val email: String = "",
    val avatar: String? = null,
    val createdAt: String = "",
    val banned: Boolean = false,
    val videoAds: Boolean? = null,
    val videoAdsAlways: Boolean? = null,
    val contentAds: Boolean? = null,
)

data class AccountLists(val favorites: Set<Int>, val collections: Map<Int, CollectionType>)

data class AccountTimecode(val episodeId: String, val seconds: Double, val watched: Boolean)

data class AccountHistoryItem(val releaseId: Int, val episodeId: String, val updatedAt: Long)

data class DeviceCode(val code: String, val remainingSeconds: Int)
