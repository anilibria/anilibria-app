package ru.radiationx.anilibria

import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import toothpick.InjectConstructor

@InjectConstructor
class MobileCheckerSources : CheckerReserveSources {

    override val sources: List<String> = listOf(
        "https://github.com/anilibria/anilibria-app/raw/master/check.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json"
    )
}