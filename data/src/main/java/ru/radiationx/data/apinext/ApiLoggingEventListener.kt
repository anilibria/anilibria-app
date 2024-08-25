package ru.radiationx.data.apinext

import android.os.SystemClock
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException


class ApiLoggingEventListener private constructor() : EventListener() {

    private val callSegment = Segment("Call")
    private val requestHeadersSegment = Segment("RequestHeaders")
    private val requestBodySegment = Segment("RequestBody")
    private val responseHeadersSegment = Segment("ResponseHeaders")
    private val responseBodySegment = Segment("ResponseBody")

    override fun callStart(call: Call) {
        callSegment.start()
        callSegment.log("${call.request().method} ${call.request().url}")
    }


    override fun requestHeadersStart(call: Call) {
        requestHeadersSegment.start()
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        requestHeadersSegment.end()
    }


    override fun requestBodyStart(call: Call) {
        requestBodySegment.start()
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        if (byteCount > 0) {
            requestBodySegment.end()
        }
    }


    override fun requestFailed(call: Call, ioe: IOException) {
        requestHeadersSegment.end()
        requestBodySegment.end()
    }


    override fun responseHeadersStart(call: Call) {
        responseHeadersSegment.start()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        responseHeadersSegment.end()
    }


    override fun responseBodyStart(call: Call) {
        responseBodySegment.start()
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        if (byteCount > 0) {
            responseBodySegment.end()
        }
    }


    override fun responseFailed(call: Call, ioe: IOException) {
        responseHeadersSegment.end()
        responseBodySegment.end()
    }


    override fun callEnd(call: Call) {
        callSegment.end()
        logSegments()
    }

    override fun callFailed(call: Call, ioe: IOException) {
        callSegment.fail(ioe)
        logSegments()
    }


    override fun canceled(call: Call) {
    }

    private fun logSegments() {
        val message = buildString {
            appendLine("${callSegment.name} [${callSegment.elapsed()} ms] ${callSegment.logs()}")
            segmentLine(requestHeadersSegment)
            segmentLine(requestBodySegment)
            segmentLine(responseHeadersSegment)
            segmentLine(responseBodySegment)
        }
        if (callSegment.exception != null) {
            Timber.e(callSegment.exception, message)
        } else {
            Timber.i(message)
        }
    }

    private fun StringBuilder.segmentLine(target: Segment) {
        if (target.start == 0L) {
            //appendLine("${target.name} not called")
        } else {
            appendLine("[${target.elapsed(callSegment)} ms] ${target.name} (${target.elapsed()} ms)")
        }
    }

    class Segment(val name: String) {
        var start = 0L
            private set
        var end = 0L
            private set
        var exception: IOException? = null
            private set

        private val logs = mutableListOf<String>()
        fun logs(): List<String> = logs
        fun log(message: String) {
            logs.add(message)
        }

        fun start() {
            start = SystemClock.elapsedRealtime()
        }

        fun end() {
            if (end != 0L) return
            end = SystemClock.elapsedRealtime()
        }

        fun fail(exception: IOException) {
            end()
            this.exception = exception
        }

        fun elapsed(): Long {
            return end - start
        }

        fun elapsed(origin: Segment): LongRange {
            return (start - origin.start)..(end - origin.start)
        }

    }

    open class Factory : EventListener.Factory {
        override fun create(call: Call): EventListener = ApiLoggingEventListener()
    }
}