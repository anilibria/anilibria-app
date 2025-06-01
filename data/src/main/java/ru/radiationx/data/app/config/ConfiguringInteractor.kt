package ru.radiationx.data.app.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.ConfiguringAnalytics
import ru.radiationx.data.analytics.features.model.AnalyticsConfigState
import ru.radiationx.data.app.config.models.ApiAddress
import ru.radiationx.data.app.config.models.ConfigScreenState
import timber.log.Timber
import javax.inject.Inject

class ConfiguringInteractor @Inject constructor(
    private val apiConfig: ApiConfig,
    private val configurationRepository: ConfigurationRepository,
    private val analytics: ConfiguringAnalytics,
) {

    private val initialState = State.LOAD_CONFIG

    private val screenState = MutableStateFlow(ConfigScreenState())

    private var currentState = initialState

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val fullTimeCounter = TimeCounter()

    private var isFullSuccess = false
    private var startAddressTag = apiConfig.tag

    fun observeScreenState(): Flow<ConfigScreenState> = screenState

    fun initCheck() {
        startAddressTag = apiConfig.tag
        fullTimeCounter.start()
        updateState(initialState)
        doByState()
    }

    fun repeatCheck() {
        analytics.onRepeatClick(currentState.toAnalyticsState())
        doByState()
    }

    fun nextCheck() {
        analytics.onNextStepClick(currentState.toAnalyticsState())
        val nextState = getNextState() ?: initialState
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
        State.LOAD_CONFIG -> loadConfig()
        State.CHECK_AVAIL -> checkAvail()
        State.CHECK_PROXIES -> checkProxies()
    }

    private fun getNextState(anchor: State = currentState): State? = when (anchor) {
        State.LOAD_CONFIG -> State.CHECK_AVAIL
        State.CHECK_AVAIL -> State.CHECK_PROXIES
        State.CHECK_PROXIES -> null
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
                val proxies = addresses.sumOf { it.proxies.size }
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
                    it.copy(status = "Найден доступный адрес")
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

    @OptIn(ExperimentalCoroutinesApi::class)
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
                            Pair(proxy, configurationRepository.getPingHost(proxy.ip))
                        )
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

    private fun State.toAnalyticsState(): AnalyticsConfigState = when (this) {
        State.LOAD_CONFIG -> AnalyticsConfigState.LOAD_CONFIG
        State.CHECK_AVAIL -> AnalyticsConfigState.CHECK_AVAIL
        State.CHECK_PROXIES -> AnalyticsConfigState.CHECK_PROXIES
    }

    private enum class State {
        LOAD_CONFIG,
        CHECK_AVAIL,
        CHECK_PROXIES
    }
}