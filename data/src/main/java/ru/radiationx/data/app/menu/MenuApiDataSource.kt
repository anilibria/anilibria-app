package ru.radiationx.data.app.menu

import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.menu.remote.LinkMenuResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class MenuApiDataSource @Inject constructor(
    private val api: DirectApi
) {

    private val urls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/menu-config.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/menu-config.json"
    )

    suspend fun getMenu(): List<LinkMenuResponse> {
        return urls.sequentialFirstNotFailure { url ->
            api.getMenuConfig(url)
        }
    }

}