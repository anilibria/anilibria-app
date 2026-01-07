package ru.radiationx.data.network.errors

import okio.IOException
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.Type

class RetrofitCallErrorAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> {
        val nextCallAdapter =
            retrofit.nextCallAdapter(this, returnType, annotations) as CallAdapter<Any, Call<Any>>
        return CallAdapterWrapper(nextCallAdapter)
    }

    private class CallAdapterWrapper(
        private val nextCallAdapter: CallAdapter<Any, Call<Any>>
    ) : CallAdapter<Any, Call<Any>> {

        override fun responseType(): Type {
            return nextCallAdapter.responseType()
        }

        override fun adapt(call: Call<Any>): Call<Any> {
            val nextCall = nextCallAdapter.adapt(call)
            return CallWrapper(nextCall)
        }
    }

    private class CallWrapper(
        private val call: Call<Any>
    ) : Call<Any> by call {

        override fun enqueue(callback: Callback<Any>) {
            call.enqueue(CallBackWrapper(callback))
        }
    }

    private class CallBackWrapper(
        private val callback: Callback<Any>
    ) : Callback<Any> by callback {

        override fun onResponse(call: Call<Any>, response: Response<Any>) {
            if (response.isSuccessful) {
                callback.onResponse(call, response)
            } else {
                val request = call.request()
                val context = NetworkErrorContext.of(request, response.raw(), null)
                val error = RetrofitCallException(context, HttpException(response))
                callback.onFailure(call, error)
            }
        }

        override fun onFailure(call: Call<Any>, t: Throwable) {
            val error = if (t is IOException) {
                t
            } else {
                val request = call.request()
                val context = NetworkErrorContext.of(request, null, null)
                RetrofitCallException(context, t)
            }
            callback.onFailure(call, error)
        }
    }

}