package ru.radiationx.anilibria.presentation.configuring

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.repository.ConfigurationRepository
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class ConfiguringPresenter @Inject constructor(
        private val router: Router,
        private val apiConfig: ApiConfig,
        private val configurationRepository: ConfigurationRepository,
        private val schedulers: SchedulersProvider
) : BasePresenter<ConfiguringView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.showStatus("Загрузка данных")
        loadConfig()
        //checkAvail()
        //checkProxies()
    }

    fun loadConfig() {
        configurationRepository
                .getConfiguration()
                .observeOn(schedulers.ui())
                .subscribe({
                    val addresses = it.size
                    val proxies = it.sumBy { it.proxies.size }
                    viewState.showStatus("Загружено адресов: $addresses; прокси: $proxies")
                    checkAvail()
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun checkAvail() {
        val addresses = apiConfig.getAddresses()
        Observable
                .fromIterable(addresses)
                .concatMapSingle { address ->
                    configurationRepository.checkAvailable(address.api).map { Pair(address, it) }
                }
                .filter { it.second }
                .map { it.first }
                .toList()
                .observeOn(schedulers.ui())
                .subscribe({
                    viewState.showStatus("Доступнные адреса: ${it.size}")
                    Log.e("boboob", "checkAvail ${it.joinToString()}")
                    if (it.isNotEmpty()) {
                        apiConfig.updateActiveAddress(it.first())
                        apiConfig.updateNeedConfig(false)
                    } else {
                        checkProxies()
                    }
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

    fun checkProxies() {
        val proxies = apiConfig.getAddresses().map { it.proxies }.reduce { acc, list -> acc.plus(list) }
        Observable
                .fromIterable(proxies)
                .concatMapSingle { proxy ->
                    configurationRepository.getPingHost(proxy.ip).map { Pair(proxy, it) }
                }
                .filter { !it.second.hasError() }
                .map { it.first }
                .toList()
                .observeOn(schedulers.ui())
                .subscribe({
                    viewState.showStatus("Доступнные прокси: ${it.size}")
                    Log.e("bobobo", "subscribe proxy $it")
                }, {
                    it.printStackTrace()
                })
                .addToDisposable()
    }

}