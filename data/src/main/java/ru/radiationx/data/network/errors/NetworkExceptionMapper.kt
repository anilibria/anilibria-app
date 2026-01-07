package ru.radiationx.data.network.errors

fun Throwable.causeOfNetwork():Throwable{
    return when(this){
        is OkHttpException,
        is RetrofitCallException -> this.cause!!
        else -> this
    }
}