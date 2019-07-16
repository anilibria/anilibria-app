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
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class ConfiguringPresenter @Inject constructor(
        private val router: Router,
        private val apiConfig: ApiConfig,
        private val configurationRepository: ConfigurationRepository,
        private val schedulers: SchedulersProvider,
        private val errorHandler: IErrorHandler
) : BasePresenter<ConfiguringView>(router) {

    private var currentState = State.CHECK_LAST

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        doByState()
    }

    fun continueCheck() = doByState()

    fun skipCheck() {
        apiConfig.updateNeedConfig(false)
    }

    private fun doByState() = when (currentState) {
        State.CHECK_LAST -> checkLast()
        State.LOAD_CONFIG -> loadConfig()
        State.CHECK_AVAIL -> checkAvail()
        State.CHECK_PROXIES -> checkProxies()
    }

    private fun checkLast() {
        currentState = State.CHECK_LAST
        configurationRepository
                .checkAvailable(apiConfig.apiUrl)
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    viewState.showStatus("Проверка доступности сервера")
                    viewState.showRefresh(false)
                }
                .subscribe({
                    if (it) {
                        viewState.showStatus("Сервер доступен")
                        apiConfig.updateNeedConfig(false)
                    } else {
                        loadConfig()
                    }
                }, {
                    it.printStackTrace()
                    viewState.showStatus("Ошибка проверки доступности сервера: ${it.message}".also { Log.e("bobobo", it) })
                    viewState.showRefresh(true)
                })
                .addToDisposable()
    }

    private fun loadConfig() {
        currentState = State.LOAD_CONFIG
        configurationRepository
                .getConfiguration()
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    viewState.showStatus("Загрузка списка адресов")
                    viewState.showRefresh(false)
                }
                .subscribe({
                    val addresses = apiConfig.getAddresses()
                    val proxies = addresses.sumBy { it.proxies.size }
                    viewState.showStatus("Загружено адресов: ${addresses.size}; прокси: $proxies".also { Log.e("bobobo", it) })
                    checkAvail()
                }, {
                    it.printStackTrace()
                    viewState.showStatus("Ошибка загрузки данных: ${it.message}".also { Log.e("bobobo", it) })
                    viewState.showRefresh(true)
                })
                .addToDisposable()
    }

    private fun checkAvail() {
        currentState = State.CHECK_AVAIL
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
                .doOnSubscribe {
                    viewState.showStatus("Проверка доступных адресов")
                    viewState.showRefresh(false)
                }
                .subscribe({
                    viewState.showStatus("Доступнные адреса: ${it.size}".also { Log.e("bobobo", it) })
                    Log.e("boboob", "checkAvail ${it.joinToString()}")
                    apiConfig.setAvailableAddresses(it)
                    if (it.isNotEmpty()) {
                        apiConfig.updateActiveAddress(it.random())
                        apiConfig.updateNeedConfig(false)
                    } else {
                        checkProxies()
                    }
                }, {
                    it.printStackTrace()
                    viewState.showStatus("Ошибка проверки доступности адресов: ${it.message}".also { Log.e("bobobo", it) })
                    viewState.showRefresh(true)
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun checkProxies() {
        currentState = State.CHECK_PROXIES
        val proxies = apiConfig.getAddresses().map { it.proxies }.reduce { acc, list -> acc.plus(list) }
        Observable
                .fromIterable(proxies)
                .concatMapSingle { proxy ->
                    configurationRepository.getPingHost(proxy.ip).map { Pair(proxy, it) }
                }
                .filter { !it.second.hasError() }
                .toList()
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    viewState.showStatus("Проверка доступных прокси")
                    viewState.showRefresh(false)
                }
                .subscribe({
                    it.forEach {
                        apiConfig.setProxyPing(it.first, it.second.timeTaken)
                    }
                    val bestProxy = it.minBy { it.second.timeTaken }
                    val addressByProxy = apiConfig.getAddresses().find { it.proxies.contains(bestProxy?.first) }
                    if (bestProxy != null && addressByProxy != null) {
                        apiConfig.updateActiveAddress(addressByProxy)
                        viewState.showStatus("Доступнные прокси: ${it.size}; будет использован ${bestProxy.first.tag} адреса ${addressByProxy.tag}".also { Log.e("bobobo", it) })
                    }

                    apiConfig.updateNeedConfig(false)
                }, {
                    it.printStackTrace()
                    viewState.showStatus("Ошибка проверки доступности прокси-серверов: ${it.message}".also { Log.e("bobobo", it) })
                    viewState.showRefresh(true)
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private enum class State {
        CHECK_LAST,
        LOAD_CONFIG,
        CHECK_AVAIL,
        CHECK_PROXIES
    }

}