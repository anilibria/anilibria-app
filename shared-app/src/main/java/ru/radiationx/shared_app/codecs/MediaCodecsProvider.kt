package ru.radiationx.shared_app.codecs

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import ru.radiationx.shared_app.codecs.types.*

object MediaCodecsProvider {

    val codecs by lazy {
        getCodecsList().filter { it.supportedTypes.isNotEmpty() }.map {
            val mimeTypes = it.supportedTypes.toList()
            val firstMimeType = mimeTypes.first()
            val outputType = when {
                firstMimeType.startsWith("audio") -> CodecOutputType.AUDIO
                firstMimeType.startsWith("video") -> CodecOutputType.VIDEO
                else -> CodecOutputType.UNKNOWN
            }
            val isSoftwareCodec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                !it.isHardwareAccelerated
            } else {
                isSoftwareCodec(it.name)
            }
            val processingType = if (isSoftwareCodec) {
                CodecProcessingType.SOFTWARE
            } else {
                CodecProcessingType.HARDWARE
            }
            val type = if (it.isEncoder) {
                CodecType.ENCODER
            } else {
                CodecType.DECODER
            }
            val knownCodec = MediaCodecsFinder.getKnown(it.name, mimeTypes)
            Codec(it.name, mimeTypes, type, outputType, processingType, knownCodec)
        }
    }

    @Suppress("DEPRECATION")
    private fun getCodecsList(): List<MediaCodecInfo> {
        return (0 until MediaCodecList.getCodecCount()).map {
            MediaCodecList.getCodecInfoAt(it)
        }
    }

    private fun isSoftwareCodec(name: String): Boolean {
        return name.startsWith("OMX.google.") || name.startsWith("c2.android.")
    }
}