package ru.radiationx.anilibria.ui.fragments.other

data class ProfileScreenState(
    val profile: ProfileItemState? = null,
    val menuItems: List<List<OtherMenuItemState>> = emptyList()
)

data class ProfileItemState(
    val id: Int,
    val hasAuth: Boolean,
    val title: String,
    val subtitle: String?,
    val avatar: String
)

data class OtherMenuItemState(
    val id: Int,
    val title: String,
    val iconRes: Int
)