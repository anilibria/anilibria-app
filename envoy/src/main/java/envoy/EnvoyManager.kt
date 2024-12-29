package envoy

import androidx.collection.SparseArrayCompat

class EnvoyManager<T> {

    private val envoys = SparseArrayCompat<Envoy<T>>()

    fun addEnvoy(envoy: Envoy<T>) {
        require(envoys.get(envoy.getViewType()) == null) {
            "Already has envoy for viewType=${envoy.getViewType()}"
        }
        envoys.put(envoy.getViewType(), envoy)
    }

    fun getItemViewType(item: T): Int {
        val count = envoys.size()
        for (index in 0 until count) {
            val envoy = envoys.valueAt(index)
            if (envoy.isForViewType(item)) {
                return envoys.keyAt(index)
            }
        }
        error("Can't find envoy for item=$item")
    }

    fun getByViewType(viewType: Int): Envoy<T> {
        return requireNotNull(envoys[viewType]) {
            "Can't find envoy for viewType=$viewType"
        }
    }
}