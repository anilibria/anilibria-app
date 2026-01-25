package ru.radiationx.shared_app.controllers.loaderpage

inline fun <T, R> PageLoaderParams<T>.appendData(block: (T?) -> R): R {
    return if (isFirstPage) {
        block.invoke(null)
    } else {
        block.invoke(currentData)
    }
}

inline fun <T, R> PageLoaderParams<T>.toDataAction(
    hasMoreData: Boolean? = null,
    block: (T?) -> R
): PageLoaderAction.Data<R> {
    val data = appendData(block)
    return PageLoaderAction.Data(data, hasMoreData)
}
