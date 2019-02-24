package ru.radiationx.anilibria.entity.app.auth

data class SocialAuth(
        val key: String,
        val title: String,
        val socialUrl: String,
        val resultPattern: String,
        val errorUrlPattern: String
) {
    companion object {
        const val KEY_VK = "vk"
        const val KEY_PATREON = "patreon"
    }
}