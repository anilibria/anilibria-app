package ru.radiationx.data.extensions

import io.reactivex.Single
import ru.radiationx.data.entity.common.DataWrapper

fun <T> Single<DataWrapper<T>>.nullOnError() =
    this.onErrorReturn { DataWrapper(null) }

fun <T> T.toWrapper() = DataWrapper(this)