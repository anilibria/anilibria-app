package ru.radiationx.anilibria.ui.fragments.release.details

import ru.radiationx.data.common.Url

data class ReleasePagerState(
    val poster: Url.Path? = null,
    val title: String? = null,
    val loading: Boolean = false
)