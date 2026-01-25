package envoy

import android.view.ViewGroup

abstract class Envoy<out T>(
    private val itemClass: Class<T>
) {

    fun getViewType(): Int {
        return itemClass.hashCode()
    }

    fun isForViewType(item: @UnsafeVariance T): Boolean {
        return itemClass.isInstance(item)
    }

    abstract fun onCreate(parent: ViewGroup): EnvoyPresenter<@UnsafeVariance T>

    fun onBind(
        presenter: EnvoyPresenter<@UnsafeVariance T>,
        item: @UnsafeVariance T
    ) {
        presenter.bind(item)
    }

    fun onAttach(presenter: EnvoyPresenter<@UnsafeVariance T>) {
        presenter.attach()
    }

    fun onDetach(presenter: EnvoyPresenter<@UnsafeVariance T>) {
        presenter.detach()
    }
}