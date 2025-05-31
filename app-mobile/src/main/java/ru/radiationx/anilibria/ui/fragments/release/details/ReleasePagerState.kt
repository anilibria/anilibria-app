package ru.radiationx.anilibria.ui.fragments.release.details

import ru.radiationx.data.entity.common.Url

data class ReleasePagerState(
    val poster: Url.Relative? = null,
    val title: String? = null,
    val loading: Boolean = false
)