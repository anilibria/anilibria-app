package ru.radiationx.data.interactors

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.ConfiguringAnalytics
import ru.radiationx.data.analytics.features.model.AnalyticsConfigState
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.data.repository.ConfigurationRepository
import ru.radiationx.data.system.WrongHostException
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.net.ssl.*

class ConfiguringInteractor @Inject constructor(
    private val apiConfig: ApiConfig,
    private val configurationRepository: ConfigurationRepository,
    private val analytics: ConfiguringAnalytics
) {

    private val screenState = MutableStateFlow(ConfigScreenState())

    private var currentState = State.CHECK_LAST

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val fullTimeCounter = TimeCounter()

    private var isFullSuccess = false
    private var startAddressTag = apiConfig.tag

    fun observeScreenState(): Flow<ConfigScreenState> = screenState

    fun initCheck() {
        startAddressTag = apiConfig.tag
        fullTimeCounter.start()
        currentState = State.CHECK_LAST
        updateState(currentState)
        doByState()
    }

    fun repeatCheck() {
        analytics.onRepeatClick(currentState.toAnalyticsState())
        doByState()
    }

    fun nextCheck() {
        analytics.onNextStepClick(currentState.toAnalyticsState())
        val nextState = getNextState() ?: State.CHECK_LAST
        doByState(nextState)
    }

    fun skipCheck() {
        scope.launch {
            isFullSuccess = false
            analytics.onSkipClick(currentState.toAnalyticsState())
            apiConfig.updateNeedConfig(false)
        }
    }

    fun finishCheck() {
        fullTimeCounter.pause()
        analytics.checkFull(
            startAddressTag,
            apiConfig.tag,
            fullTimeCounter.elapsed(),
            isFullSuccess
        )
        scope.cancel()
    }


    private fun updateState(newState: State) {
        currentState = newState
        screenState.update {
            it.copy(hasNext = getNextState() != null)
        }
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
        val timeCounter = TimeCounter()


        scope.launch {
            timeCounter.start()
            screenState.update {
                it.copy(
                    status = "Проверка доступности сервера",
                    needRefresh = false
                )
            }
            runCatching {
                zipLastCheck()
            }.onSuccess {
                analytics.checkLast(apiConfig.tag, timeCounter.elapsed(), it, null)
                if (it) {
                    isFullSuccess = true
                    screenState.update {
                        it.copy(status = "Сервер доступен")
                    }

                    apiConfig.updateNeedConfig(false)
                } else {
                    loadConfig()
                }
            }.onFailure { error ->
                analytics.checkLast(apiConfig.tag, timeCounter.elapsed(), false, error)
                Timber.e(error)
                when (error) {
                    is WrongHostException,
                    is TimeoutException,
                    is TimeoutCancellationException -> loadConfig()
                    is IOException,
                    is SSLException,
                    is SSLHandshakeException,
                    is SSLKeyException,
                    is SSLProtocolException,
                    is SSLPeerUnverifiedException -> loadConfig()
                    else -> {
                        screenState.update {
                            it.copy(
                                status = "Ошибка проверки доступности сервера: ${error.message}",
                                needRefresh = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadConfig() {
        updateState(State.LOAD_CONFIG)
        val timeCounter = TimeCounter()


        scope.launch {
            timeCounter.start()
            screenState.update {
                it.copy(
                    status = "Загрузка списка адресов",
                    needRefresh = false
                )
            }
            runCatching {
                configurationRepository.getConfiguration()
            }.onSuccess {
                analytics.loadConfig(timeCounter.elapsed(), true, null)
                val addresses = apiConfig.getAddresses()
                val proxies = addresses.sumBy { it.proxies.size }
                screenState.update {
                    it.copy(status = "Загружено адресов: ${addresses.size}; прокси: $proxies")
                }
                checkAvail()
            }.onFailure { error ->
                analytics.loadConfig(timeCounter.elapsed(), false, error)
                Timber.e(error)
                screenState.update {
                    it.copy(
                        status = "Ошибка загрузки списка адресов: ${error.message}",
                        needRefresh = true
                    )
                }
            }
        }
    }

    private fun checkAvail() {
        updateState(State.CHECK_AVAIL)
        val timeCounter = TimeCounter()
        val addresses = apiConfig.getAddresses()

        scope.launch {
            timeCounter.start()
            screenState.update {
                it.copy(
                    status = "Проверка доступных адресов",
                    needRefresh = false
                )
            }
            runCatching {
                mergeAvailCheck(addresses)
            }.onSuccess { activeAddress ->
                isFullSuccess = true
                analytics.checkAvail(activeAddress.tag, timeCounter.elapsed(), true, null)
                screenState.update {
                    it.copy(status = "Найдет доступный адрес")
                }
                apiConfig.updateActiveAddress(activeAddress)
                apiConfig.updateNeedConfig(false)
            }.onFailure { error ->
                analytics.checkAvail(null, timeCounter.elapsed(), false, error)
                Timber.e(error)
                when (error) {
                    // from mergeAvailCheck
                    is NoSuchElementException -> {
                        checkProxies()
                    }
                    else -> {
                        screenState.update {
                            it.copy(
                                status = "Ошибка проверки доступности адресов: ${error.message}",
                                needRefresh = true
                            )
                        }
                    }
                }
            }
            timeCounter.pause()
        }
    }

    private fun checkProxies() {
        updateState(State.CHECK_PROXIES)
        val timeCounter = TimeCounter()
        val proxies =
            apiConfig.getAddresses().map { it.proxies }.reduce { acc, list -> acc.plus(list) }


        scope.launch {
            timeCounter.start()
            screenState.update {
                it.copy(
                    status = "Проверка доступных прокси",
                    needRefresh = false
                )
            }
            runCatching {
                flowOf(proxies.toTypedArray())
                    .onEach {
                        if (it.isEmpty()) {
                            throw Exception("No proxies for adresses")
                        }
                    }
                    .flatMapLatest {
                        it.asFlow()
                    }
                    .flatMapConcat { proxy ->
                        flowOf(
                            configurationRepository.getPingHost(proxy.ip).let { Pair(proxy, it) })
                    }
                    .filter { !it.second.hasError() }
                    .toList()
            }.onSuccess { proxies ->
                proxies.forEach {
                    apiConfig.setProxyPing(it.first, it.second.timeTaken)
                }
                val bestProxy = proxies.minByOrNull { it.second.timeTaken }
                val addressByProxy =
                    apiConfig.getAddresses().find { it.proxies.contains(bestProxy?.first) }
                analytics.checkProxies(addressByProxy?.tag, timeCounter.elapsed(), true, null)
                if (bestProxy != null && addressByProxy != null) {
                    isFullSuccess = true
                    apiConfig.updateActiveAddress(addressByProxy)
                    screenState.update {
                        it.copy(
                            status = "Доступнные прокси: ${proxies.size}; будет использован ${bestProxy.first.tag} адреса ${addressByProxy.tag}"
                        )
                    }
                    apiConfig.updateNeedConfig(false)
                } else {
                    screenState.update {
                        it.copy(
                            status = "Не найдены доступные прокси",
                            needRefresh = true
                        )
                    }
                }
            }.onFailure { error ->
                analytics.checkProxies(null, timeCounter.elapsed(), false, error)
                Timber.e(error)
                screenState.update {
                    it.copy(
                        status = "Ошибка проверки доступности прокси-серверов: ${error.message}",
                        needRefresh = true
                    )
                }
            }
            timeCounter.pause()
        }
    }

    private suspend fun mergeAvailCheck(addresses: List<ApiAddress>): ApiAddress {
        val adressesSources = addresses.map { address ->
            flow { emit(configurationRepository.checkAvailable(address.api)) }
                .catch { emit(false) }
                .map { Pair(address, it) }
        }
        return merge(*adressesSources.toTypedArray())
            .filter { it.second }
            .map { it.first }
            .first()
    }

    private suspend fun zipLastCheck(): Boolean {
        val flowMain = flow { emit(configurationRepository.checkAvailable(apiConfig.apiUrl)) }
        val flowApi = flow { emit(configurationRepository.checkApiAvailable(apiConfig.apiUrl)) }
        return combine(flowMain, flowApi) { mainRes, apiRes ->
            mainRes && apiRes
        }.first()
    }

    private fun State.toAnalyticsState(): AnalyticsConfigState = when (this) {
        State.CHECK_LAST -> AnalyticsConfigState.CHECK_LAST
        State.LOAD_CONFIG -> AnalyticsConfigState.LOAD_CONFIG
        State.CHECK_AVAIL -> AnalyticsConfigState.CHECK_AVAIL
        State.CHECK_PROXIES -> AnalyticsConfigState.CHECK_PROXIES
    }

    private enum class State {
        CHECK_LAST,
        LOAD_CONFIG,
        CHECK_AVAIL,
        CHECK_PROXIES
    }
}