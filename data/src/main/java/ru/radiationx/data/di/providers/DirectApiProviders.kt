package ru.radiationx.data.di.providers

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.di.DirectClient
import ru.radiationx.data.di.DirectRetrofit
import javax.inject.Inject
import javax.inject.Provider


class DirectRetrofitProvider @Inject constructor(
    @DirectClient private val okHttpClient: OkHttpClient,
) : Provider<Retrofit> {
    override fun get(): Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://anilibria.top/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit
    }
}

class DirectApiProvider @Inject constructor(
    @DirectRetrofit private val retrofit: Retrofit
) : Provider<DirectApi> {
    override fun get(): DirectApi = retrofit.create()
}