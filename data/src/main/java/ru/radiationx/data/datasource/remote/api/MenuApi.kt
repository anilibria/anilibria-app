package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.entity.response.other.LinkMenuResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class MenuApi @Inject constructor(
    @MainClient private val client: IClient,
    private val moshi: Moshi
) {

    private val urls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/menu-config.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/menu-config.json"
    )

    suspend fun getMenu(): List<LinkMenuResponse> {
        return urls.sequentialFirstNotFailure { url ->
            client.get(url, emptyMap())
                .fetchListApiResponse(moshi)
        }
    }

}