package ru.radiationx.data.app.config

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.data.app.config.models.AppConfigAddressId
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toBaseUrl
import ru.radiationx.data.network.NetworkObserver
import javax.inject.Inject

class AppConfigImpl @Inject constructor(
    private val networkObserver: NetworkObserver
) : AppConfig {

    private val defaultAddress = AppConfigAddress(
        id = AppConfigAddressId(id = "default"),
        name = "default",
        description = null,
        widget = "https://www.anilibria.tv/".toBaseUrl(),
        site = "https://anilibria.wtf/".toBaseUrl(),
        image = "https://api.anilibria.app/".toBaseUrl(),
        api = "https://api.anilibria.app/".toBaseUrl(),
        status = "https://api.anilibria.app/api/v1/teams/roles".toAbsoluteUrl()
    )

    private val activeAddressState = MutableStateFlow<AppConfigAddress?>(null)

    private var networkHash: Int? = null

    fun needsConfigure(): Boolean {
        return networkObserver.getHash() != networkHash
    }

    fun needsUpdateConfig(): Boolean {
        return networkHash == null
    }

    fun setReady(address: AppConfigAddress) {
        networkHash = networkObserver.getHash()
        activeAddressState.value = address
    }

    fun setDefault() {
        networkHash = networkObserver.getHash()
        activeAddressState.value = defaultAddress
    }

    private val active: AppConfigAddress
        get() = activeAddressState.value ?: defaultAddress

    override val configState: Flow<Boolean>
        get() = activeAddressState.map { it != null }

    override val isConfigured: Boolean
        get() = activeAddressState.value != null

    override val id: AppConfigAddressId
        get() = active.id

    val name: String?
        get() = active.name

    override val description: String?
        get() = active.description

    override val widget: Url.Base
        get() = active.widget

    override val site: Url.Base
        get() = active.site

    override val image: Url.Base
        get() = active.image

    override val api: Url.Base
        get() = active.api

    val status: Url.Absolute
        get() = active.status

}