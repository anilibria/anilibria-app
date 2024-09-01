package ru.radiationx.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

class SuspendMutableStateFlow<T>(
    private val initialBlock: suspend () -> T
) : Flow<T> {

    private val _stateFlow = MutableStateFlow<Wrapper<T>?>(null)

    override suspend fun collect(collector: FlowCollector<T>) {
        getOrInit()
        return _stateFlow
            .filterNotNull()
            .map { it.value }
            .distinctUntilChanged()
            .collect(collector)
    }

    suspend fun getValue(): T {
        return getOrInit()
    }

    suspend fun setValue(value: T) {
        getOrInit()
        _stateFlow.value = Wrapper(value)
    }

    suspend fun update(block: (T) -> T) {
        getOrInit()
        _stateFlow.update {
            requireNotNull(it) {
                "Wrapper is null in update"
            }
            Wrapper(block.invoke(it.value))
        }
    }

    private suspend fun getOrInit(): T {
        val wrapper = _stateFlow.updateAndGet {
            it ?: Wrapper(initialBlock.invoke())
        }
        requireNotNull(wrapper) {
            "Wrapper is null after init"
        }
        return wrapper.value
    }

    private data class Wrapper<T>(
        val value: T
    )
}