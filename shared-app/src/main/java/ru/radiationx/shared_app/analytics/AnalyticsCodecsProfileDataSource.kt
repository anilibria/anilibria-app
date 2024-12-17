package ru.radiationx.shared_app.analytics

import ru.radiationx.data.analytics.profile.ProfileAttribute
import ru.radiationx.data.analytics.profile.ProfileConstants
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.codecs.MediaCodecsProvider
import ru.radiationx.shared_app.codecs.filterKnown
import ru.radiationx.shared_app.codecs.isDecoder
import ru.radiationx.shared_app.codecs.isHardware
import ru.radiationx.shared_app.codecs.isSoftware
import ru.radiationx.shared_app.codecs.types.KnownCodec
import javax.inject.Inject

class AnalyticsCodecsProfileDataSource @Inject constructor() {

    fun getAttributes(): List<ProfileAttribute> = KnownCodec.entries.map { knownCodec ->
        coRunCatching {
            val codecs = MediaCodecsProvider.codecs.filterKnown(knownCodec)
            val hasSoftware = codecs.any {
                it.isSoftware() && it.isDecoder()
            }
            val hasHardware = codecs.any {
                it.isHardware() && it.isDecoder()
            }

            ProfileAttribute.String(knownCodec.asName(), "s=$hasSoftware, h=${hasHardware}")
        }.getOrElse {
            ProfileAttribute.Error(knownCodec.asName(), it)
        }
    }

    private fun KnownCodec.asName() = when (this) {
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