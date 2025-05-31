package ru.radiationx.anilibria.ui.fragments.other

import ru.radiationx.data.entity.common.Url

data class ProfileScreenState(
    val profile: ProfileItemState = ProfileItemState(false, "", null, null),
    val profileMenuGroups: List<OtherMenuItemState> = emptyList(),
    val menuGroups: List<List<OtherMenuItemState>> = emptyList()
)

data class ProfileItemState(
    val hasAuth: Boolean,
    val title: String,
    val subtitle: String?,
    val avatar: Url?
)

data class OtherMenuItemState(
    val id: Int,
    val title: String,
    val iconRes: Int
)