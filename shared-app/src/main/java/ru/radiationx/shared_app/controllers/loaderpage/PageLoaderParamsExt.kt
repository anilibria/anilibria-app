package ru.radiationx.shared_app.controllers.loaderpage

fun <T> PageLoaderParams<T>.appendData(block: (T?) -> T): T {
    return if (isFirstPage) {
        block.invoke(null)
    } else {
        block.invoke(currentData)
    }
}