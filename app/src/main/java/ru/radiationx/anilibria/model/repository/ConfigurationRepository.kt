package ru.radiationx.anilibria.model.repository

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.stealthcopter.networktools.Ping
import com.stealthcopter.networktools.ping.PingOptions
import com.stealthcopter.networktools.ping.PingResult
import com.stealthcopter.networktools.ping.PingTools
import io.reactivex.Single
import ru.radiationx.anilibria.model.data.remote.address.ApiAddress
import ru.radiationx.anilibria.model.data.remote.api.ConfigurationApi
import ru.radiationx.anilibria.model.data.storage.ApiConfigStorage
import ru.radiationx.anilibria.model.system.SchedulersProvider
import java.lang.Exception
import java.net.InetAddress
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(
        private val configurationApi: ConfigurationApi,
        private val schedulers: SchedulersProvider
) {
    private val pingRelay = BehaviorRelay.create<Map<String, PingResult>>()

    fun checkAvailable(apiUrl: String): Single<Boolean> = configurationApi
            .checkAvailable(apiUrl)
            .subscribeOn(schedulers.io())

    fun getConfiguration(): Single<List<ApiAddress>> = configurationApi
            .getConfiguration()
            .subscribeOn(schedulers.io())

    fun getPingHost(host: String): Single<PingResult> = Single
            .fromCallable {
                PingTools.doNativePing(InetAddress.getByName(host), PingOptions())
            }
            .doOnSuccess {
                val map = if (pingRelay.hasValue()) {
                    pingRelay.value!!.toMutableMap()
                } else {
                    mutableMapOf()
                }
                map[host] = it
                pingRelay.accept(map)
            }
            .subscribeOn(schedulers.io())
}