package ru.radiationx.data.interactors

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.analytics.features.ConfiguringAnalytics
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.model.AnalyticsConfigState
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.data.repository.ConfigurationRepository
import ru.radiationx.data.system.WrongHostException
import ru.radiationx.shared.ktx.addTo
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.net.ssl.*

class ConfiguringInteractor @Inject constructor(
    private val apiConfig: ApiConfig,
    private val configurationRepository: ConfigurationRepository,
    private val schedulers: SchedulersProvider,
    private val analytics: ConfiguringAnalytics
) {

    private val subject = BehaviorSubject.create<ConfigScreenState>()

    private var currentState = State.CHECK_LAST

    private val screenState = ConfigScreenState()

    private val compositeDisposable = CompositeDisposable()

    private val fullTimeCounter = TimeCounter()

    private var isFullSuccess = false
    private var startAddressTag = apiConfig.tag

    fun observeScreenState(): Observable<ConfigScreenState> = subject.hide()

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
        isFullSuccess = false
        analytics.onSkipClick(currentState.toAnalyticsState())
        apiConfig.updateNeedConfig(false)
    }

    fun finishCheck() {
        fullTimeCounter.pause()
        analytics.checkFull(
            startAddressTag,
            apiConfig.tag,
            fullTimeCounter.elapsed(),
            isFullSuccess
        )
        compositeDisposable.clear()
    }

    private fun notifyScreenChanged() {
        subject.onNext(screenState)
    }

    private fun updateState(newState: State) {
        currentState = newState
        screenState.hasNext = getNextState() != null
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
        val timeCounter = TimeCounter()
        compositeDisposable.clear()
        zipLastCheck()
            .observeOn(schedulers.ui())
            .doOnSubscribe {
                timeCounter.start()
                screenState.status = "Проверка доступности сервера"
                screenState.needRefresh = false
                notifyScreenChanged()
            }
            .doFinally { timeCounter.pause() }
            .subscribe({
                analytics.checkLast(apiConfig.tag, timeCounter.elapsed(), it, null)
                if (it) {
                    isFullSuccess = true
                    screenState.status = "Сервер доступен"
                    notifyScreenChanged()
                    apiConfig.updateNeedConfig(false)
                } else {
                    loadConfig()
                }
            }, {
                analytics.checkLast(apiConfig.tag, timeCounter.elapsed(), false, it)
                it.printStackTrace()
                when (it) {
                    is WrongHostException,
                    is TimeoutException -> loadConfig()
                    is IOException,
                    is SSLException,
                    is SSLHandshakeException,
                    is SSLKeyException,
                    is SSLProtocolException,
                    is SSLPeerUnverifiedException -> loadConfig()
                    else -> {
                        screenState.status = "Ошибка проверки доступности сервера: ${it.message}"
                        screenState.needRefresh = true
                        notifyScreenChanged()
                    }
                }
            })
            .addTo(compositeDisposable)
    }

    private fun loadConfig() {
        updateState(State.LOAD_CONFIG)
        val timeCounter = TimeCounter()
        compositeDisposable.clear()
        configurationRepository
            .getConfiguration()
            .observeOn(schedulers.ui())
            .doOnSubscribe {
                timeCounter.start()
                screenState.status = "Загрузка списка адресов"
                screenState.needRefresh = false
                notifyScreenChanged()
            }
            .doFinally { timeCounter.pause() }
            .subscribe({
                analytics.loadConfig(timeCounter.elapsed(), true, null)
                val addresses = apiConfig.getAddresses()
                val proxies = addresses.sumBy { it.proxies.size }
                screenState.status = "Загружено адресов: ${addresses.size}; прокси: $proxies"
                notifyScreenChanged()
                checkAvail()
            }, {
                analytics.loadConfig(timeCounter.elapsed(), false, it)
                it.printStackTrace()
                screenState.status =
                    "Ошибка загрузки списка адресов: ${it.message}"
                screenState.needRefresh = true
                notifyScreenChanged()
            })
            .addTo(compositeDisposable)
    }

    private fun checkAvail() {
        updateState(State.CHECK_AVAIL)
        val timeCounter = TimeCounter()
        compositeDisposable.clear()
        val addresses = apiConfig.getAddresses()
        mergeAvailCheck(addresses)
            .observeOn(schedulers.ui())
            .doOnSubscribe {
                timeCounter.start()
                screenState.status = "Проверка доступных адресов"
                screenState.needRefresh = false
                notifyScreenChanged()
            }
            .doFinally { timeCounter.pause() }
            .subscribe({ activeAddress ->
                isFullSuccess = true
                analytics.checkAvail(activeAddress.tag, timeCounter.elapsed(), true, null)
                screenState.status = "Найдет доступный адрес"
                notifyScreenChanged()
                apiConfig.updateActiveAddress(activeAddress)
                apiConfig.updateNeedConfig(false)
            }, {
                analytics.checkAvail(null, timeCounter.elapsed(), false, it)
                it.printStackTrace()
                when (it) {
                    // from mergeAvailCheck
                    is NoSuchElementException -> {
                        checkProxies()
                    }
                    else -> {
                        screenState.status = "Ошибка проверки доступности адресов: ${it.message}"
                        screenState.needRefresh = true
                        notifyScreenChanged()
                    }
                }
            })
            .addTo(compositeDisposable)
    }

    private fun checkProxies() {
        updateState(State.CHECK_PROXIES)
        val timeCounter = TimeCounter()
        compositeDisposable.clear()
        val proxies =
            apiConfig.getAddresses().map { it.proxies }.reduce { acc, list -> acc.plus(list) }
        Observable
            .fromArray(proxies)
            .doOnNext {
                if (it.isEmpty()) {
                    throw Exception("No proxies for adresses")
                }
            }
            .flatMap { Observable.fromIterable(it) }
            .concatMapSingle { proxy ->
                configurationRepository.getPingHost(proxy.ip).map { Pair(proxy, it) }
            }
            .filter { !it.second.hasError() }
            .toList()
            .observeOn(schedulers.ui())
            .doOnSubscribe {
                timeCounter.start()
                screenState.status = "Проверка доступных прокси"
                screenState.needRefresh = false
                notifyScreenChanged()
            }
            .doFinally { timeCounter.pause() }
            .subscribe({
                it.forEach {
                    apiConfig.setProxyPing(it.first, it.second.timeTaken)
                }
                val bestProxy = it.minByOrNull { it.second.timeTaken }
                val addressByProxy =
                    apiConfig.getAddresses().find { it.proxies.contains(bestProxy?.first) }
                analytics.checkProxies(addressByProxy?.tag, timeCounter.elapsed(), true, null)
                if (bestProxy != null && addressByProxy != null) {
                    isFullSuccess = true
                    apiConfig.updateActiveAddress(addressByProxy)
                    screenState.status =
                        "Доступнные прокси: ${it.size}; будет использован ${bestProxy.first.tag} адреса ${addressByProxy.tag}"
                    notifyScreenChanged()
                    apiConfig.updateNeedConfig(false)
                } else {
                    screenState.status = "Не найдены доступные прокси"
                    screenState.needRefresh = true
                    notifyScreenChanged()
                }
            }, {
                analytics.checkProxies(null, timeCounter.elapsed(), false, it)
                it.printStackTrace()
                screenState.status =
                    "Ошибка проверки доступности прокси-серверов: ${it.message}"
                screenState.needRefresh = true
                notifyScreenChanged()
            })
            .addTo(compositeDisposable)
    }

    private fun mergeAvailCheck(addresses: List<ApiAddress>): Single<ApiAddress> {
        val adressesSources = addresses.map { address ->
            configurationRepository.checkAvailable(address.api)
                .subscribeOn(schedulers.io())
                .onErrorReturnItem(false)
                .map { Pair(address, it) }
        }
        return Single
            .merge(adressesSources)
            .filter { it.second }
            .map { it.first }
            .firstOrError()
    }

    private fun zipLastCheck(): Single<Boolean> = Single.zip(
        configurationRepository
            .checkAvailable(apiConfig.apiUrl)
            .subscribeOn(schedulers.io()),
        configurationRepository
            .checkApiAvailable(apiConfig.apiUrl)
            .subscribeOn(schedulers.io()),
        BiFunction<Boolean, Boolean, Boolean> { result1, result2 ->
            return@BiFunction result1 && result2
        }
    )

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