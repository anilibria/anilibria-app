package ru.radiationx.anilibria

import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import javax.inject.Inject

class MobileCheckerSources @Inject constructor() : CheckerReserveSources {

    override val sources: List<String> = listOf(
        "https://github.com/anilibria/anilibria-app/raw/master/check.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json"
    )
}