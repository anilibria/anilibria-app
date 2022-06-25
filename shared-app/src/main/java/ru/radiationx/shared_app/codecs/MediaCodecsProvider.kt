package ru.radiationx.shared_app.codecs

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import ru.radiationx.shared_app.codecs.types.Codec
import ru.radiationx.shared_app.codecs.types.CodecOutputType
import ru.radiationx.shared_app.codecs.types.CodecProcessingType
import ru.radiationx.shared_app.codecs.types.CodecType

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
            val processingType = if (isSoftwareCodec(it.name)) {
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

    private fun getCodecsList(): List<MediaCodecInfo> {
        return (0 until MediaCodecList.getCodecCount()).map {
            MediaCodecList.getCodecInfoAt(it)
        }
    }

    private fun isSoftwareCodec(name: String): Boolean {
        return name.startsWith("OMX.google.")
                || name.startsWith("c2.android.")
                || (!name.startsWith("OMX.")
                && !name.startsWith("c2."))
    }
}