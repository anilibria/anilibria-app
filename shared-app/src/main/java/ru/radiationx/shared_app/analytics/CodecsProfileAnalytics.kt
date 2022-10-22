package ru.radiationx.shared_app.analytics

import ru.radiationx.data.analytics.profile.ProfileConstants
import ru.radiationx.shared_app.codecs.*
import ru.radiationx.shared_app.codecs.types.KnownCodec
import toothpick.InjectConstructor

@InjectConstructor
class CodecsProfileAnalytics {

    suspend fun getCodecsInfo(): Map<String, String> {
        return KnownCodec.values().associate { knownCodec ->
            val codecs = MediaCodecsProvider.codecs.filterKnown(knownCodec)
            val hasSoftware = codecs.any {
                it.isSoftware() && it.isDecoder()
            }
            val hasHardware = codecs.any {
                it.isHardware() && it.isDecoder()
            }
            knownCodec.asConstant() to "s=$hasSoftware, h=${hasHardware}"
        }
    }

    private fun KnownCodec.asConstant() = when (this) {
        KnownCodec.AUDIO_AAC -> ProfileConstants.codec_audio_aac
        KnownCodec.AUDIO_OPUS -> ProfileConstants.codec_audio_opus
        KnownCodec.AUDIO_MP3 -> ProfileConstants.codec_audio_mp3
        KnownCodec.AUDIO_VORBIS -> ProfileConstants.codec_audio_vorbis
        KnownCodec.VIDEO_AVC -> ProfileConstants.codec_video_avc
        KnownCodec.VIDEO_HEVC -> ProfileConstants.codec_video_hevc
        KnownCodec.VIDEO_AV1 -> ProfileConstants.codec_video_av1
        KnownCodec.VIDEO_VP9 -> ProfileConstants.codec_video_vp9
    }
}