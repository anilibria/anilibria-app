package ru.radiationx.anilibria.presentation.configuring

import android.util.Log
import io.reactivex.Observable
import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.repository.ConfigurationRepository
import ru.radiationx.data.system.WrongHostException
import ru.terrakok.cicerone.Router
import java.io.IOException
import javax.inject.Inject
import javax.net.ssl.*

@InjectViewState
class ConfiguringPresenter @Inject constructor(
        private val router: Router,
        private val apiConfig: ApiConfig,
        private val configurationRepository: ConfigurationRepository,
        private val schedulers: SchedulersProvider,
        private val errorHandler: IErrorHandler
) : BasePresenter<ConfiguringView>(router) {

    private var currentState = State.CHECK_LAST

    private val screenState = ScreenState()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        updateState(currentState)
        doByState()
    }

    fun continueCheck() = doByState()

    fun nextCheck() {
        getNextState()?.also { doByState(it) }
    }

    fun skipCheck() {
        apiConfig.updateNeedConfig(false)
    }


    private fun notifyScreenChanged() {
        viewState.updateScreen(screenState)
    }

    private fun updateState(newState: State) {
        currentState = newState
        if (getNextState() == null) {
            screenState.nextButton = null
        } else {
            screenState.nextButton = "Следующий шаг"
        }
        notifyScreenChanged()
    }

    private fun doByState(anchor: State = currentState) = when (anchor) {
        State.CHECK_LAST -> checkLast()
        State.LOAD_CONFIG -> loadConfig()
        State.CHECK_AVAIL -> checkAvail()
        State.CHECK_PROXIES -> checkProxies()
    }

    private fun getNextState(anchor: State = currentState): State? = when (anchor) {
        State.CHECK_LAST -> State.LOAD_CONFIG
        State.LOAD_CONFIG -> State.CHECK_AVAIL
        State.CHECK_AVAIL -> State.CHECK_PROXIES
        State.CHECK_PROXIES -> null
    }

    private fun getTitleByState(state: State?): String? = when (state) {
        State.CHECK_LAST -> "Проверка текущих адресов"
        State.LOAD_CONFIG -> "Загрузка новых адресов"
        State.CHECK_AVAIL -> "Проверка новых адресов"
        State.CHECK_PROXIES -> "Проверка прокси-серверов"
        else -> null
    }

    private fun checkLast() {
        updateState(State.CHECK_LAST)
        Log.e("bobobo", "active address ${apiConfig.active}")
        Log.e("bobobo", "getAddresses ${apiConfig.getAddresses()}")
        Log.e("bobobo", "getAvailableAddresses ${apiConfig.getAvailableAddresses()}")
        configurationRepository
                .checkAvailable(apiConfig.apiUrl)
                .flatMap { configurationRepository.checkApiAvailable(apiConfig.apiUrl) }
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    screenState.status = "Проверка доступности сервера"
                    screenState.refresh = false
                    notifyScreenChanged()
                }
                .subscribe({
                    if (it) {
                        screenState.status = "Сервер доступен"
                        notifyScreenChanged()
                        apiConfig.updateNeedConfig(false)
                    } else {
                        loadConfig()
                    }
                }, {
                    Log.e("bobobo", "error on $currentState: $it, ${it is IOException}")
                    it.printStackTrace()
                    when (it) {
                        is WrongHostException -> loadConfig()
                        is IOException,
                        is SSLException,
                        is SSLHandshakeException,
                        is SSLKeyException,
                        is SSLProtocolException,
                        is SSLPeerUnverifiedException -> loadConfig()
                        else -> {
                            screenState.status = "Ошибка проверки доступности сервера: ${it.message}".also { Log.e("bobobo", it) }
                            screenState.refresh = true
                            notifyScreenChanged()
                        }
                    }
                })
                .addToDisposable()
    }

    private fun loadConfig() {
        updateState(State.LOAD_CONFIG)
        configurationRepository
                .getConfiguration()
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    screenState.status = "Загрузка списка адресов"
                    screenState.refresh = false
                    notifyScreenChanged()
                }
                .subscribe({
                    val addresses = apiConfig.getAddresses()
                    val proxies = addresses.sumBy { it.proxies.size }
                    screenState.status = "Загружено адресов: ${addresses.size}; прокси: $proxies".also { Log.e("bobobo", it) }
                    notifyScreenChanged()
                    checkAvail()
                }, {
                    Log.e("bobobo", "error on $currentState: $it")
                    it.printStackTrace()
                    screenState.status = "Ошибка загрузки списка адресов: ${it.message}".also { Log.e("bobobo", it) }
                    screenState.refresh = true
                    notifyScreenChanged()
                })
                .addToDisposable()
    }

    private fun checkAvail() {
        updateState(State.CHECK_AVAIL)
        val addresses = apiConfig.getAddresses()
        Observable
                .fromIterable(addresses)
                .concatMapSingle { address ->
                    configurationRepository.checkAvailable(address.api)
                            .onErrorReturnItem(false)
                            .map { Pair(address, it) }
                }
                .filter { it.second }
                .map { it.first }
                .toList()
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    screenState.status = "Проверка доступных адресов"
                    screenState.refresh = false
                    notifyScreenChanged()
                }
                .subscribe({
                    screenState.status = "Доступнные адреса: ${it.size}".also { Log.e("bobobo", it) }
                    notifyScreenChanged()
                    Log.e("boboob", "checkAvail ${it.joinToString()}")
                    apiConfig.setAvailableAddresses(it)
                    if (it.isNotEmpty()) {
                        apiConfig.updateActiveAddress(it.random())
                        apiConfig.updateNeedConfig(false)
                    } else {
                        checkProxies()
                    }
                }, {
                    Log.e("bobobo", "error on $currentState: $it")
                    it.printStackTrace()
                    screenState.status = "Ошибка проверки доступности адресов: ${it.message}".also { Log.e("bobobo", it) }
                    screenState.refresh = true
                    notifyScreenChanged()
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun checkProxies() {
        updateState(State.CHECK_PROXIES)
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
                    screenState.status = "Проверка доступных прокси"
                    screenState.refresh = false
                    notifyScreenChanged()
                }
                .subscribe({
                    it.forEach {
                        apiConfig.setProxyPing(it.first, it.second.timeTaken)
                    }
                    val bestProxy = it.minBy { it.second.timeTaken }
                    val addressByProxy = apiConfig.getAddresses().find { it.proxies.contains(bestProxy?.first) }
                    if (bestProxy != null && addressByProxy != null) {
                        apiConfig.updateActiveAddress(addressByProxy)
                        screenState.status = "Доступнные прокси: ${it.size}; будет использован ${bestProxy.first.tag} адреса ${addressByProxy.tag}".also { Log.e("bobobo", it) }
                        notifyScreenChanged()
                    }

                    apiConfig.updateNeedConfig(false)
                }, {
                    Log.e("bobobo", "error on $currentState: $it")
                    it.printStackTrace()
                    screenState.status = "Ошибка проверки доступности прокси-серверов: ${it.message}".also { Log.e("bobobo", it) }
                    screenState.refresh = true
                    notifyScreenChanged()
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

    class ScreenState(
            var status: String = "",
            var refresh: Boolean = false,
            var nextButton: String? = null
    )

}