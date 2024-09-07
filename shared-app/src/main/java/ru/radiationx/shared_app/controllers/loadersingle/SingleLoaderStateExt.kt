package ru.radiationx.shared_app.controllers.loadersingle

fun <T> SingleLoaderState<T>.needShowError(): Boolean {
    return error != null && !loading
}

fun <T> SingleLoaderState<T>.needShowEmpty(block: (T?) -> Boolean = { it == null }): Boolean {
    val dataCheck = data.let(block)
    return dataCheck && error == null && !loading
}

fun <T> SingleLoaderState<List<T>>.needShowListEmpty(): Boolean {
    return needShowEmpty { it?.isEmpty() == true }
}

fun <T> SingleLoaderState<T>.needShowData(block: (T?) -> Boolean = { it != null }): Boolean {
    val dataCheck = data.let(block)
    return dataCheck && error == null && !loading
}

fun <T> SingleLoaderState<List<T>>.needShowListData(): Boolean {
    return needShowData { it?.isNotEmpty() == true }
}

fun <T, R> SingleLoaderState<T>.mapData(block: (T) -> R): SingleLoaderState<R> {
    return SingleLoaderState(
        data = data?.let { block.invoke(it) },
        error = error,
        loading = loading
    )
}

fun <T> SingleLoaderState<T>.mapOtherLoading(otherLoading: Boolean): SingleLoaderState<T> {
    return copy(loading = loading || otherLoading)
}